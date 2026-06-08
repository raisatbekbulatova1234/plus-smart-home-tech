package ru.yandex.practicum.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.exceptions.client.PaymentClientException;
import ru.yandex.practicum.exceptions.client.PaymentServiceUnavailableException;
import ru.yandex.practicum.exceptions.client.ServiceValidationException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.payment.PaymentNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Фасад для взаимодействия с сервисом оплаты (Payment Service)
 *
 * Отвечает за:
 * - Создание платежей
 * - Обработку успешных/неуспешных платежей
 * - Расчет стоимости товаров и полной стоимости заказа
 *
 * Использует Circuit Breaker для защиты от сбоев сервиса оплаты
 */
@Service
@RequiredArgsConstructor
public class PaymentClientOrderFacade {

    private final PaymentClient paymentClient;     // Feign клиент для вызова сервиса оплаты
    private final ObjectMapper objectMapper;

    // ==================== СОЗДАНИЕ ПЛАТЕЖА ====================

    /**
     * Создание нового платежа для заказа
     */
    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "createPaymentFallback")
    public PaymentDto createPayment(OrderDtoPayment request) {
        try {
            return paymentClient.createPayment(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    // ==================== ОБРАБОТКА ПЛАТЕЖЕЙ ====================

    /**
     * Уведомление об успешном завершении платежа
     */
    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "handlePaymentFallback")
    public void handleSuccessfulPayment(UUID paymentId) {
        try {
            paymentClient.handleSuccessfulPayment(paymentId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    /**
     * Уведомление о неуспешном платеже (ошибка, отмена)
     */
    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "handlePaymentFallback")
    public void handleFailedPayment(UUID paymentId) {
        try {
            paymentClient.handleFailedPayment(paymentId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    // ==================== РАСЧЕТЫ ====================

    /**
     * Расчет стоимости товаров в заказе
     */
    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "calculatePriceFallback")
    public BigDecimal calculateProductPrice(OrderDtoPayment request) {
        try {
            return paymentClient.calculateProductPrice(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    /**
     * Расчет полной стоимости заказа (товары + доставка)
     */
    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "calculatePriceFallback")
    public BigDecimal calculateTotalPrice(OrderDtoPayment request) {
        try {
            return paymentClient.calculateTotalPrice(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    // ==================== FALLBACK МЕТОДЫ (Circuit Breaker) ====================

    /**
     * Fallback при создании платежа
     * Вызывается при недоступности сервиса оплаты
     */
    public PaymentDto createPaymentFallback(OrderDtoPayment request, Throwable t) {
        throw paymentUnavailable(t);
    }

    /**
     * Fallback при обработке платежа
     * Вызывается при недоступности сервиса оплаты
     */
    public void handlePaymentFallback(UUID paymentId, Throwable t) {
        throw paymentUnavailable(t);
    }

    /**
     * Fallback при расчетах
     * Вызывается при недоступности сервиса оплаты
     */
    public BigDecimal calculatePriceFallback(OrderDtoPayment request, Throwable t) {
        throw paymentUnavailable(t);
    }

    // ==================== ОБРАБОТКА ОШИБОК FEIGN ====================

    /**
     * Обработка исключений Feign клиента
     * Преобразует HTTP ошибки в бизнес-исключения
     */
    private RuntimeException handleFeignException(FeignException e) {
        try {
            // Десериализуем тело ошибки из JSON
            ErrorResponse error = objectMapper.readValue(e.contentUTF8(), ErrorResponse.class);

            return switch (e.status()) {
                // HTTP 400 - Bad Request
                case 400 -> {
                    if (ErrorCodes.VALIDATION_FAILED.equals(error.getError())) {
                        // Ошибка валидации входных данных
                        yield new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                    }
                    // Другая ошибка клиента
                    yield new PaymentClientException(error.getMessage(), e);
                }

                // HTTP 404 - Not Found
                case 404 -> {
                    if (ErrorCodes.PAYMENT_NOT_FOUND.equals(error.getError())) {
                        // Платеж не найден
                        yield new PaymentNotFoundException(error.getMessage());
                    }
                    if (ErrorCodes.PRODUCT_IN_STORE_NOT_FOUND.equals(error.getError())) {
                        // Товар не найден в магазине
                        yield new ProductNotFoundException(error.getMessage());
                    }
                    yield new PaymentClientException(error.getMessage(), e);
                }

                // Другие HTTP статусы (500, 503 и т.д.)
                default -> new PaymentClientException(
                        "Неожиданный ответ сервиса оплаты. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            // Не удалось разобрать JSON ответ
            return new PaymentClientException(
                    "Ошибка разбора ответа сервиса оплаты.", ex
            );
        }
    }

    /**
     * Создание исключения о недоступности сервиса оплаты
     */
    private PaymentServiceUnavailableException paymentUnavailable(Throwable t) {
        return new PaymentServiceUnavailableException(
                ErrorCodes.PAYMENT_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}