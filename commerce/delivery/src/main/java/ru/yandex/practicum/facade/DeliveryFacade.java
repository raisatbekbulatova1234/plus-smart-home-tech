package ru.yandex.practicum.facade;

import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryFacade {
    DeliveryDto createDelivery(NewDeliveryDto request);

    void handleSuccessfulDelivery(UUID deliveryId);

    void handlePickedDelivery(UUID deliveryId);

    void handleFailedDelivery(UUID deliveryId);

    BigDecimal calculateDeliveryCost(OrderDtoDelivery request);
}
