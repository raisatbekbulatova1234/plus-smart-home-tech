package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.facade.PaymentFacade;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentClient {
    private final PaymentFacade paymentFacade;

    @PostMapping
    public PaymentDto createPayment(@RequestBody @Valid OrderDtoPayment request) {
        log.info("Получен POST-запрос на оплату заказа с id {}.",
                request.getOrderId());
        return paymentFacade.createPayment(request);
    }

    @PostMapping("/totalCost")
    public BigDecimal calculateTotalPrice(@RequestBody @Valid OrderDtoPayment request) {
        log.info("Получен POST-запрос на расчет полной стоимости заказа с id {}.",
                request.getOrderId());
        return paymentFacade.calculateTotalPrice(request);
    }

    @PostMapping("/productCost")
    public BigDecimal calculateProductPrice(@RequestBody @Valid OrderDtoPayment request) {
        log.info("Получен POST-запрос на расчет стоимости товаров в заказе с id {}.",
                request.getOrderId());
        return paymentFacade.calculateProductPrice(request);
    }

    @PostMapping("/refund")
    public void handleSuccessfulPayment(@RequestBody UUID paymentId) {
        log.info("Получен POST-запрос на успешную проводку платежа с id {}.", paymentId);
        paymentFacade.handleSuccessfulPayment(paymentId);
    }

    @PostMapping("/failed")
    public void handleFailedPayment(@RequestBody UUID paymentId) {
        log.info("Получен POST-запрос на ошибку в проводке платежа с id {}.", paymentId);
        paymentFacade.handleFailedPayment(paymentId);
    }
}
