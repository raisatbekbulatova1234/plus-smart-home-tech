package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.ProductPrice;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
/**
 * Сервис для управления платежами
 * Содержит бизнес-логику создания и обработки платежей
 */
public interface PaymentService {
    PaymentDto createPayment(OrderDtoPayment request);

    BigDecimal calculateTotalPrice(OrderDtoPayment request);

    BigDecimal calculateProductPrice(List<ProductPrice> itemsWithPrice);

    void handleSuccessfulPayment(UUID paymentId);

    void handleFailedPayment(UUID paymentId);
}