package com.darius.wms.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 50)
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "may contain only letters, digits and dashes")
        String sku,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull UnitOfMeasure unit,
        @DecimalMin("0.0") @Digits(integer = 7, fraction = 3) BigDecimal weightKg,
        @NotNull @Min(0) Integer minStockLevel) {
}
