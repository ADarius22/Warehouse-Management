# ---- Stage 1: build the jar with the full JDK ---------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only the build files first: the dependency download is cached by Docker
# and only re-runs when pom.xml changes, not on every code change.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---- Stage 2: small runtime image with only the JRE and the jar --------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Do not run the application as root
RUN groupadd --system wms && useradd --system --gid wms wms
USER wms

COPY --from=build /app/target/wms-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
