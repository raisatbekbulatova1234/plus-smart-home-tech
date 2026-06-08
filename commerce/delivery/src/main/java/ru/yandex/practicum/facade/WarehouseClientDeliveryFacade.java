package ru.yandex.practicum.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.exceptions.client.ServiceValidationException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.OrderBookingNotFoundException;
import ru.yandex.practicum.exceptions.client.WarehouseClientException;
import ru.yandex.practicum.exceptions.client.WarehouseServiceUnavailableException;

@Service
@RequiredArgsConstructor
public class WarehouseClientDeliveryFacade {
    private final WarehouseClient warehouseClient;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "warehouseDelivery", fallbackMethod = "shipProductsToDeliveryFallback")
    public void shipProductsToDelivery(ShippedToDeliveryRequest request) {
        try {

            warehouseClient.shipProductsToDelivery(request);

        } catch (FeignException.NotFound e) {

            try {

                ErrorResponse error = objectMapper.readValue(e.contentUTF8(), ErrorResponse.class);

                if (ErrorCodes.VALIDATION_FAILED.equals(error.getError())) {
                    throw new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                }

                if (ErrorCodes.ORDER_BOOKING_NOT_FOUND.equals(error.getError())) {
                    throw new OrderBookingNotFoundException(error.getMessage());
                }

                throw new WarehouseClientException("Неожиданная ошибка обработки ответа сервиса склада.", e);

            } catch (JsonProcessingException ex) {

                throw new WarehouseClientException("Неожиданная ошибка ответа сервиса склада.", ex);
            }
        }
    }

    public void shipProductsToDeliveryFallback(ShippedToDeliveryRequest request, Throwable t) {
        throw new WarehouseServiceUnavailableException(ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}
