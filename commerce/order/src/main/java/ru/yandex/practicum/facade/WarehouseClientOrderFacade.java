package ru.yandex.practicum.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.order.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exceptions.client.ServiceValidationException;
import ru.yandex.practicum.exceptions.client.WarehouseClientException;
import ru.yandex.practicum.exceptions.client.WarehouseServiceUnavailableException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.OrderBookingAlreadyExistsException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseClientOrderFacade {
    private final WarehouseClient warehouseClient;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "warehouseOrder", fallbackMethod = "returnProductsFallback")
    public void returnProducts(Map<UUID, Long> products) {
        try {
            warehouseClient.returnProducts(products);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "warehouseOrder", fallbackMethod = "assembleOrderFallback")
    public BookedProductsDto assembleOrder(AssemblyProductsForOrderRequest request) {
        try {
            return warehouseClient.assembleOrder(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    @CircuitBreaker(name = "warehouseOrder", fallbackMethod = "getAddressFallback")
    public AddressDto getAddress() {
        try {
            return warehouseClient.getAddress();
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

                    if (ErrorCodes.ORDER_BOOKING_ALREADY_EXIST.equals(error.getError())) {
                        yield new OrderBookingAlreadyExistsException(error.getMessage());
                    }

                    yield new WarehouseClientException(error.getMessage(), e);
                }

                case 404 -> {
                    if (ErrorCodes.NO_PRODUCT_IN_WAREHOUSE.equals(error.getError())) {
                        yield new NoSpecifiedProductInWarehouseException(error.getMessage());
                    }
                    yield new WarehouseClientException(error.getMessage(), e);
                }

                default -> new WarehouseClientException(
                        "Неожиданный ответ сервиса склада. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            return new WarehouseClientException(
                    "Ошибка разбора ответа сервиса склада.", ex
            );
        }
    }

    public void returnProductsFallback(Map<UUID, Long> products, Throwable t) {
        throw warehouseUnavailable(t);
    }

    public BookedProductsDto assembleOrderFallback(AssemblyProductsForOrderRequest request, Throwable t) {
        throw warehouseUnavailable(t);
    }

    public AddressDto getAddressFallback(Throwable t) {
        throw warehouseUnavailable(t);
    }

    private WarehouseServiceUnavailableException warehouseUnavailable(Throwable t) {
        return new WarehouseServiceUnavailableException(
                ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}
