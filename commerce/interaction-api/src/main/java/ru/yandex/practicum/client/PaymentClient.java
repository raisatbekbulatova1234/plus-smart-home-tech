package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feign клиент для взаимодействия с сервисом оплаты (Payment Service)
 *
 * Используется сервисом заказов для:
 * - Создания платежей
 * - Расчетов стоимости товаров и доставки
 * - Обработки успешных/неуспешных платежей
 *
 * name = "payment" - имя сервиса в Eureka (Service Discovery)
 * path = "/api/v1/payment" - базовый путь для всех запросов к сервису
 */
@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {

    // ==================== СОЗДАНИЕ ПЛАТЕЖА ====================

    /**
     * Создание нового платежа для заказа
     */
    @PostMapping
    PaymentDto createPayment(@RequestBody @Valid OrderDtoPayment request);

    // ==================== РАСЧЕТЫ ====================

    /**
     * Расчет полной стоимости заказа (товары + доставка)
     */
    @PostMapping("/totalCost")
    BigDecimal calculateTotalPrice(@RequestBody @Valid OrderDtoPayment request);

    /**
     * Расчет стоимости товаров в заказе (без доставки)
     */
    @PostMapping("/productCost")
    BigDecimal calculateProductPrice(@RequestBody @Valid OrderDtoPayment request);

    // ==================== ОБРАБОТКА ПЛАТЕЖЕЙ ====================

    /**
     * Обработка успешного платежа
     */
    @PostMapping("/refund")
    void handleSuccessfulPayment(@RequestBody UUID paymentId);

    /**
     * Обработка неуспешного платежа (отмена, ошибка)
     */
    @PostMapping("/failed")
    void handleFailedPayment(@RequestBody UUID paymentId);
}