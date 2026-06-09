package ru.yandex.practicum.facade;

import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentFacade {
    PaymentDto createPayment(OrderDtoPayment request);

    BigDecimal calculateTotalPrice(OrderDtoPayment request);

    BigDecimal calculateProductPrice(OrderDtoPayment request);

    void handleSuccessfulPayment(UUID paymentId);

    void handleFailedPayment(UUID paymentId);
}
