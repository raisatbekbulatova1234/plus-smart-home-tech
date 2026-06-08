package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookedProductsDto {
    @NotNull
    @DecimalMin("1.0")
    Double deliveryWeight;

    @NotNull
    @DecimalMin("1.0")
    Double deliveryVolume;

    @NotNull
    Boolean fragile;
}
