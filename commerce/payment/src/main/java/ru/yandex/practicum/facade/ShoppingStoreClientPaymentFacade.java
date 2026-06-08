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

/**
 * Фасад для взаимодействия с сервисом магазина (Shopping Store Service)
 *
 * Отвечает за:
 * - Получение информации о товарах (цены, описание)
 *
 * Использует Circuit Breaker для защиты от сбоев сервиса магазина
 */
@Service
@RequiredArgsConstructor
public class ShoppingStoreClientPaymentFacade {

    private final ShoppingStoreClient shoppingStoreClient;  // Feign клиент магазина
    private final ObjectMapper objectMapper;                // JSON маппер для парсинга ошибок

    // ==================== ПОЛУЧЕНИЕ ТОВАРА ====================

    /**
     * Получение информации о товаре по ID
     * Используется для получения актуальной цены товара
     */
    @CircuitBreaker(name = "shoppingStorePayment", fallbackMethod = "getProductByIdFallback")
    public ProductDto getProductById(UUID productId) {
        try {
            return shoppingStoreClient.getProductById(productId);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
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
                        // Ошибка валидации
                        yield new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                    }
                    // Другая ошибка клиента
                    yield new ShoppingStoreClientException(error.getMessage(), e);
                }

                // HTTP 404 - Not Found
                case 404 -> {
                    if (ErrorCodes.PRODUCT_IN_STORE_NOT_FOUND.equals(error.getError())) {
                        // Товар не найден в магазине
                        yield new ProductNotFoundException(error.getMessage());
                    }
                    yield new ShoppingStoreClientException(error.getMessage(), e);
                }

                // Другие HTTP статусы (500, 503 и т.д.)
                default -> new ShoppingStoreClientException(
                        "Неожиданный ответ сервиса магазина. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            // Не удалось разобрать JSON ответ
            return new WarehouseClientException(
                    "Ошибка разбора ответа сервиса магазина.", ex
            );
        }
    }

    // ==================== FALLBACK МЕТОД (Circuit Breaker) ====================

    /**
     * Fallback для получения товара
     * Вызывается при недоступности сервиса магазина
     */
    public ProductDto getProductByIdFallback(UUID productId, Throwable t) {
        throw new ShoppingStoreServiceUnavailableException(
                ErrorCodes.SHOPPING_STORE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}