package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class NewProductInWarehouseRequest {
    @NotNull
    UUID productId;

    @NotNull
    Boolean fragile;

    @Valid
    DimensionDto dimension;

    @NotNull
    @DecimalMin("1.0")
    Double weight;
}
