package ru.yandex.practicum.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exceptions.client.ServiceValidationException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exceptions.client.WarehouseClientException;
import ru.yandex.practicum.exceptions.client.WarehouseServiceUnavailableException;

@Service
@RequiredArgsConstructor
public class WarehouseClientCartFacade {
    private final WarehouseClient warehouseClient;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "warehouseCart", fallbackMethod = "checkShoppingCartFallback")
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart) {
        try {

            return warehouseClient.checkShoppingCart(shoppingCart);

        } catch (FeignException.BadRequest e) {

            try {

                ErrorResponse error = objectMapper.readValue(e.contentUTF8(), ErrorResponse.class);

                if (ErrorCodes.VALIDATION_FAILED.equals(error.getError())) {
                    throw new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                }

                if (ErrorCodes.LOW_QUANTITY_IN_WAREHOUSE.equals(error.getError())) {
                    throw new ProductInShoppingCartLowQuantityInWarehouse(error.getMessage());
                }

                if (ErrorCodes.NO_PRODUCT_IN_WAREHOUSE.equals(error.getError())) {
                    throw new NoSpecifiedProductInWarehouseException(error.getMessage());
                }

                throw new WarehouseClientException("Неожиданная ошибка обработки ответа сервиса склада.", e);

            } catch (JsonProcessingException ex) {

                throw new WarehouseClientException("Неожиданная ошибка ответа сервиса склада.", ex);
            }
        }
    }

    public BookedProductsDto checkShoppingCartFallback(ShoppingCartDto shoppingCart, Throwable t) {
        throw new WarehouseServiceUnavailableException(ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}
