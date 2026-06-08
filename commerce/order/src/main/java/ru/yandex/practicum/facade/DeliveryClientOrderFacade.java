package ru.yandex.practicum.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;
import ru.yandex.practicum.exceptions.client.*;
import ru.yandex.practicum.exceptions.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryClientOrderFacade {
    private final DeliveryClient deliveryClient;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "deliveryOrder", fallbackMethod = "handleDeliveryFallback")
    public void handlePickedDelivery(UUID deliveryId) {
        try {
            deliveryClient.handlePickedDelivery(deliveryId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "deliveryOrder", fallbackMethod = "handleDeliveryFallback")
    public void handleSuccessfulDelivery(UUID deliveryId) {
        try {
            deliveryClient.handleSuccessfulDelivery(deliveryId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "deliveryOrder", fallbackMethod = "handleDeliveryFallback")
    public void handleFailedDelivery(UUID deliveryId) {
        try {
            deliveryClient.handleFailedDelivery(deliveryId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "deliveryOrder", fallbackMethod = "calculateDeliveryCostFallback")
    public BigDecimal calculateDeliveryCost(OrderDtoDelivery request) {
        try {
            return deliveryClient.calculateDeliveryCost(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "deliveryOrder", fallbackMethod = "createDeliveryFallback")
    public DeliveryDto createDelivery(NewDeliveryDto dto) {
        try {
            return deliveryClient.createDelivery(dto);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
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

                    yield new DeliveryClientException(error.getMessage(), e);
                }

                case 404 -> {
                    if (ErrorCodes.DELIVERY_NOT_FOUND.equals(error.getError())) {
                        yield new NoDeliveryFoundException(error.getMessage());
                    }
                    yield new DeliveryClientException(error.getMessage(), e);
                }

                default -> new DeliveryClientException(
                        "Неожиданный ответ сервиса доставки. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            return new DeliveryClientException("Ошибка разбора ответа сервиса доставки.", ex);
        }
    }

    public void handleDeliveryFallback(UUID deliveryId, Throwable t) {
        throw deliveryUnavailable(t);
    }

    public BigDecimal calculateDeliveryCostFallback(OrderDtoDelivery request, Throwable t) {
        throw deliveryUnavailable(t);
    }

    public DeliveryDto createDeliveryFallback(NewDeliveryDto dto, Throwable t) {
        throw deliveryUnavailable(t);
    }

    private DeliveryServiceUnavailableException deliveryUnavailable(Throwable t) {
        return new DeliveryServiceUnavailableException(
                ErrorCodes.DELIVERY_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}
