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
import ru.yandex.practicum.exceptions.store.ProductNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentClientOrderFacade {
    private final PaymentClient paymentClient;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "createPaymentFallback")
    public PaymentDto createPayment(OrderDtoPayment request) {
        try {
            return paymentClient.createPayment(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "handlePaymentFallback")
    public void handleSuccessfulPayment(UUID paymentId) {
        try {
            paymentClient.handleSuccessfulPayment(paymentId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "handlePaymentFallback")
    public void handleFailedPayment(UUID paymentId) {
        try {
            paymentClient.handleFailedPayment(paymentId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "calculatePriceFallback")
    public BigDecimal calculateProductPrice(OrderDtoPayment request) {
        try {
            return paymentClient.calculateProductPrice(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "paymentOrder", fallbackMethod = "calculatePriceFallback")
    public BigDecimal calculateTotalPrice(OrderDtoPayment request) {
        try {
            return paymentClient.calculateTotalPrice(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    public PaymentDto createPaymentFallback(OrderDtoPayment request, Throwable t) {
        throw paymentUnavailable(t);
    }

    public void handlePaymentFallback(UUID paymentId, Throwable t) {
        throw paymentUnavailable(t);
    }

    public BigDecimal calculatePriceFallback(OrderDtoPayment request, Throwable t) {
        throw paymentUnavailable(t);
    }

    private RuntimeException handleFeignException(FeignException e) {

        try {
            ErrorResponse error =
                    objectMapper.readValue(e.contentUTF8(), ErrorResponse.class);

            return switch (e.status()) {

                case 400 -> {
                    if (ErrorCodes.VALIDATION_FAILED.equals(error.getError())) {
                        yield new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                    }

                    yield new PaymentClientException(error.getMessage(), e);
                }

                case 404 -> {
                    if (ErrorCodes.PAYMENT_NOT_FOUND.equals(error.getError())) {
                        yield new PaymentNotFoundException(error.getMessage());
                    }

                    if (ErrorCodes.PRODUCT_IN_STORE_NOT_FOUND.equals(error.getError())) {
                        yield new ProductNotFoundException(error.getMessage());
                    }

                    yield new PaymentClientException(error.getMessage(), e);
                }

                default -> new PaymentClientException(
                        "Неожиданный ответ сервиса оплаты. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            return new PaymentClientException(
                    "Ошибка разбора ответа сервиса оплаты.", ex
            );
        }
    }

    private PaymentServiceUnavailableException paymentUnavailable(Throwable t) {
        return new PaymentServiceUnavailableException(
                ErrorCodes.PAYMENT_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}
