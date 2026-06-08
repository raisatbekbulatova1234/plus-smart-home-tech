package ru.yandex.practicum.dto.order;

import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
/**
 * Контекст заказа - содержит всю информацию о заказе
 * Используется для передачи между сервисами и этапами обработки
 */
@Value
@Builder(toBuilder = true)
public class OrderContext {
    UUID orderId;
    UUID shoppingCartId;
    String username;
    AddressDto toAddress;
    AddressDto fromAddress;
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