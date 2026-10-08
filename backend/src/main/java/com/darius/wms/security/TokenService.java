package com.darius.wms.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Creates signed JWT access tokens after a successful login. */
@Service
public class TokenService {

    static final String ISSUER = "wms-backend";
    static final String ROLES_CLAIM = "roles";

    private final JwtEncoder encoder;
    private final Duration validity;

    public TokenService(JwtEncoder encoder, @Value("${app.jwt.validity:8h}") Duration validity) {
        this.encoder = encoder;
        this.validity = validity;
    }

    public IssuedToken issue(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(validity);
        // Authorities are "ROLE_ADMIN"; the token carries just "ADMIN"
        List<String> roles = CurrentUser.roles(authentication);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(ROLES_CLAIM, roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, authentication.getName(), roles, expiresAt);
    }

    public record IssuedToken(String token, String username, List<String> roles, Instant expiresAt) {
    }
}
