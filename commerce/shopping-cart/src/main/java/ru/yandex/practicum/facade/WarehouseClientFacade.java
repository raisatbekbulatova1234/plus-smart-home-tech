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
import ru.yandex.practicum.exceptions.cart.CartValidationException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exceptions.warehouse.WarehouseServiceUnavailableException;

/**
 * Фасад для взаимодействия с сервисом склада
 *
 * Обеспечивает:
 * - Отказоустойчивость (Circuit Breaker)
 * - Обработку ошибок Feign клиента
 * - Преобразование ошибок в бизнес-исключения
 */
@Service
@RequiredArgsConstructor
public class WarehouseClientFacade {

    private final WarehouseClient warehouseClient;  // Feign клиент для склада
    private final ObjectMapper objectMapper;        // Для парсинга ошибок

    /**
     * Проверка корзины через сервис склада
     *
     * @CircuitBreaker - защита от каскадных отказов
     * При падении сервиса склада срабатывает fallback метод
     */
    @CircuitBreaker(name = "warehouse", fallbackMethod = "checkShoppingCartFallback")
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart) {
        try {
            // Вызов сервиса склада через Feign
            return warehouseClient.checkShoppingCart(shoppingCart);

        } catch (FeignException.BadRequest e) {
            // Обработка HTTP 400
            try {
                // Парсим тело ошибки из JSON
                ErrorResponse error = objectMapper.readValue(e.contentUTF8(), ErrorResponse.class);

                // Преобразуем ошибки склада в бизнес-исключения
                if (ErrorCodes.LOW_QUANTITY_IN_WAREHOUSE.equals(error.getError())) {
                    throw new ProductInShoppingCartLowQuantityInWarehouse(error.getMessage());
                }

                if (ErrorCodes.NO_PRODUCT_IN_WAREHOUSE.equals(error.getError())) {
                    throw new NoSpecifiedProductInWarehouseException(error.getMessage());
                }

                // Неизвестная ошибка валидации
                throw new CartValidationException("Ошибка проверки корзины сервисом склада.");

            } catch (JsonProcessingException ex) {
                // Ошибка парсинга JSON от склада
                throw new CartValidationException("Ошибка обработки ответа сервиса склада.");
            }
        }
    }

    /**
     * Fallback метод для Circuit Breaker
     * Вызывается при недоступности сервиса склада
     */
    public BookedProductsDto checkShoppingCartFallback(ShoppingCartDto shoppingCart, Throwable t) {
        throw new WarehouseServiceUnavailableException(ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}