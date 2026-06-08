package ru.yandex.practicum.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Реализация фасада доставки
 * Объединяет бизнес-логику с вызовами внешних сервисов (склад)
 */
@Service
@RequiredArgsConstructor
public class DeliveryFacadeImpl implements DeliveryFacade {

    private final DeliveryService service;
    private final WarehouseClientDeliveryFacade warehouseClient;

    /**
     * Создание доставки
     * Делегирует вызов сервису доставки
     */
    @Override
    public DeliveryDto createDelivery(NewDeliveryDto request) {
        return service.createDelivery(request);
    }

    /**
     * Обработка получения доставки (курьер забрал заказ)
     * 1. Обновляет статус доставки через сервис
     * 2. Уведомляет склад об отгрузке товаров
     */
    @Override
    public void handlePickedDelivery(UUID deliveryId) {
        // 1. Обновляем статус доставки на PICKED
        DeliveryDto result = service.handlePickedDelivery(deliveryId);

        // 2. Получаем ID заказа из результата
        UUID orderId = result.getOrderId();

        // 3. Создаем запрос для уведомления склада
        ShippedToDeliveryRequest request = ShippedToDeliveryRequest.builder()
                .orderId(orderId)
                .deliveryId(result.getDeliveryId())
                .build();

        // 4. Уведомляем склад об отгрузке товаров
        warehouseClient.shipProductsToDelivery(request);
    }

    /**
     * Обработка успешной доставки
     * Делегирует вызов сервису доставки
     */
    @Override
    public void handleSuccessfulDelivery(UUID deliveryId) {
        service.handleSuccessfulDelivery(deliveryId);
    }

    /**
     * Обработка неуспешной доставки
     */
    @Override
    public void handleFailedDelivery(UUID deliveryId) {
        service.handleFailedDelivery(deliveryId);
    }

    /**
     * Расчет стоимости доставки
     * Делегирует вызов сервису доставки
     */
    @Override
    public BigDecimal calculateDeliveryCost(OrderDtoDelivery request) {
        return service.calculateDeliveryCost(request);
    }
}