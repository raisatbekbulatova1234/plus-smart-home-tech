package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderContext;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.model.Order;

import java.util.UUID;

public interface OrderService {
    Page<OrderDto> getUserOrders(String username, Pageable pageable);

    OrderDto createOrder(CreateNewOrderRequest request);

    OrderContext getOrderContext(UUID orderId);

    Order updateOrder(OrderContext orderContext);
}