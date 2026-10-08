package com.darius.wms.location;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotNull Zone zone,
        @NotBlank @Size(max = 10)
        @Pattern(regexp = "[A-Za-z0-9]+", message = "may contain only letters and digits")
        String aisle,
        @NotNull @Min(1) @Max(99) Integer rack,
        @NotNull @Min(1) @Max(99) Integer shelf,
        @NotNull @Min(1) @Max(1_000_000) Integer capacity,
        Boolean active) {
}
