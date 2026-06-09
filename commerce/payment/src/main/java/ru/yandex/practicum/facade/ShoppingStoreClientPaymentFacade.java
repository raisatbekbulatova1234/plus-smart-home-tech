package ru.yandex.practicum.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.exceptions.client.*;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.store.ProductNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingStoreClientPaymentFacade {
    private final ShoppingStoreClient shoppingStoreClient;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "shoppingStorePayment", fallbackMethod = "getProductByIdFallback")
    public ProductDto getProductById(UUID productId) {
        try {
            return shoppingStoreClient.getProductById(productId);
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

                    yield new ShoppingStoreClientException(error.getMessage(), e);
                }

                case 404 -> {
                    if (ErrorCodes.PRODUCT_IN_STORE_NOT_FOUND.equals(error.getError())) {
                        yield new ProductNotFoundException(error.getMessage());
                    }
                    yield new ShoppingStoreClientException(error.getMessage(), e);
                }

                default -> new ShoppingStoreClientException(
                        "Неожиданный ответ сервиса магазина. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            return new WarehouseClientException(
                    "Ошибка разбора ответа сервиса магазина.", ex
            );
        }
    }

    public ProductDto getProductByIdFallback(UUID productId, Throwable t) {
        throw new ShoppingStoreServiceUnavailableException(ErrorCodes.SHOPPING_STORE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}
