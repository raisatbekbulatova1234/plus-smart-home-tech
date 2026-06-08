package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {
    DeliveryDto createDelivery(NewDeliveryDto request);

    DeliveryDto handlePickedDelivery(UUID deliveryId);

    void handleSuccessfulDelivery(UUID deliveryId);

    void handleFailedDelivery(UUID deliveryId);

    BigDecimal calculateDeliveryCost(OrderDtoDelivery request);
}