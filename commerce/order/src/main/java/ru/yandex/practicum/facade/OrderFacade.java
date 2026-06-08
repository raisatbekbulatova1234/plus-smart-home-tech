package ru.yandex.practicum.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.UUID;
/**
 * Фасад для управления заказами
 * Объединяет бизнес-логику и взаимодействие с внешними сервисами
 */
public interface OrderFacade {
    Page<OrderDto>  getUserOrders(String username, Pageable pageable);

    OrderDto createOrder(CreateNewOrderRequest request);

    OrderDto handleReturn(ProductReturnRequest request);

    OrderDto prepareOrderForPayment(UUID orderId);

    OrderDto handleSuccessfulPayment(UUID orderId);

    OrderDto handleFailedPayment(UUID orderId);

    OrderDto handleSuccessfulAssembly(UUID orderId);

    OrderDto handleFailedAssembly(UUID orderId);

    OrderDto handlePickedDelivery(UUID orderId);

    OrderDto handleSuccessfulDelivery(UUID orderId);

    OrderDto handleFailedDelivery(UUID orderId);

    OrderDto handleComplete(UUID orderId);

    OrderDto handleCalculateTotalPrice(UUID orderId);

    OrderDto handleCalculateDeliveryPrice(UUID orderId);
}