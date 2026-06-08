package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class OrderDtoPayment {
    @NotNull
    UUID orderId;

    @NotNull
    Map<@NotNull UUID, @Min(1) Long> products;

    UUID paymentId;
    BigDecimal totalPrice;
    BigDecimal deliveryPrice;
    BigDecimal productPrice;
}