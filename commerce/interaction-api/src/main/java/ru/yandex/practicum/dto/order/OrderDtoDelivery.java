package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import java.util.UUID;

@Value
@Builder
public class OrderDtoDelivery {
    @NotNull
    UUID orderId;

    @NotNull
    UUID deliveryId;

    @NotNull
    @DecimalMin("1.0")
    Double deliveryWeight;

    @NotNull
    @DecimalMin("1.0")
    Double deliveryVolume;

    @NotNull
    Boolean fragile;
}
