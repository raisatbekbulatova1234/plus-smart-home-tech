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

/**
 * REST контроллер для управления платежами
 * Реализует интерфейс PaymentClient для использования через Feign
 *
 * Эндпоинты:
 * - POST /api/v1/payment - создание платежа
 * - POST /api/v1/payment/totalCost - расчет полной стоимости
 * - POST /api/v1/payment/productCost - расчет стоимости товаров
 * - POST /api/v1/payment/refund - подтверждение успешного платежа
 * - POST /api/v1/payment/failed - подтверждение ошибки платежа
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentClient {

    private final PaymentFacade paymentFacade;

    /**
     * POST /api/v1/payment
     * Создание нового платежа для заказа
     */
    @PostMapping
    public PaymentDto createPayment(@RequestBody @Valid OrderDtoPayment request) {
        log.info("Получен POST-запрос на оплату заказа с id {}.",
                request.getOrderId());
        return paymentFacade.createPayment(request);
    }

    /**
     * POST /api/v1/payment/totalCost
     * Расчет полной стоимости заказа (товары + доставка)
     */
    @PostMapping("/totalCost")
    public BigDecimal calculateTotalPrice(@RequestBody @Valid OrderDtoPayment request) {
        log.info("Получен POST-запрос на расчет полной стоимости заказа с id {}.",
                request.getOrderId());
        return paymentFacade.calculateTotalPrice(request);
    }

    /**
     * POST /api/v1/payment/productCost
     * Расчет стоимости товаров в заказе (без доставки)
     */
    @PostMapping("/productCost")
    public BigDecimal calculateProductPrice(@RequestBody @Valid OrderDtoPayment request) {
        log.info("Получен POST-запрос на расчет стоимости товаров в заказе с id {}.",
                request.getOrderId());
        return paymentFacade.calculateProductPrice(request);
    }

    /**
     * POST /api/v1/payment/refund
     * Подтверждение успешного платежа
     * Обновляет статус платежа на PAID
     */
    @PostMapping("/refund")
    public void handleSuccessfulPayment(@RequestBody UUID paymentId) {
        log.info("Получен POST-запрос на успешную проводку платежа с id {}.", paymentId);
        paymentFacade.handleSuccessfulPayment(paymentId);
    }

    /**
     * POST /api/v1/payment/failed
     * Подтверждение ошибки платежа
     * Обновляет статус платежа на FAILED
     */
    @PostMapping("/failed")
    public void handleFailedPayment(@RequestBody UUID paymentId) {
        log.info("Получен POST-запрос на ошибку в проводке платежа с id {}.", paymentId);
        paymentFacade.handleFailedPayment(paymentId);
    }
}