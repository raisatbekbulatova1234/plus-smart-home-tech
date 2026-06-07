package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DimensionDto {
    @NotNull
    @DecimalMin("1.0")
    Double width;

    @NotNull
    @DecimalMin("1.0")
    Double height;

    @NotNull
    @DecimalMin("1.0")
    Double depth;
}
