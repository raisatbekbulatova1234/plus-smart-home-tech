package ru.yandex.practicum.dto.order;

import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class OrderDto {
    UUID orderId;
    UUID shoppingCartId;
    String username;
    Map<UUID, Long> products;
    UUID paymentId;
    UUID deliveryId;
    OrderState state;
    Double deliveryWeight;
    Double deliveryVolume;
    Boolean fragile;
    BigDecimal totalPrice;
    BigDecimal deliveryPrice;
    BigDecimal productPrice;
}
