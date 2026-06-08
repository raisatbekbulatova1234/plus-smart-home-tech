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

/**
 * Фасад для взаимодействия с сервисом склада (Warehouse Service)
 * из сервиса доставки (Delivery Service)
 *
 * Отвечает за:
 * - Подтверждение отгрузки товаров в доставку
 *
 * Использует Circuit Breaker для защиты от сбоев сервиса склада
 */
@Service
@RequiredArgsConstructor
public class WarehouseClientDeliveryFacade {

    private final WarehouseClient warehouseClient;
    private final ObjectMapper objectMapper;

    // ==================== ОТГРУЗКА ТОВАРОВ ====================

    /**
     * Подтверждение отгрузки товаров в доставку
     * Уведомляет склад, что товары переданы службе доставки
     */
    @CircuitBreaker(name = "warehouseDelivery", fallbackMethod = "shipProductsToDeliveryFallback")
    public void shipProductsToDelivery(ShippedToDeliveryRequest request) {
        try {
            // Вызов сервиса склада
            warehouseClient.shipProductsToDelivery(request);

        } catch (FeignException.NotFound e) {
            // Обработка HTTP 404 - Not Found
            try {
                // Парсим тело ошибки из JSON
                ErrorResponse error = objectMapper.readValue(e.contentUTF8(), ErrorResponse.class);

                // Определяем тип ошибки по ErrorCode
                if (ErrorCodes.VALIDATION_FAILED.equals(error.getError())) {
                    // Ошибка валидации
                    throw new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                }

                if (ErrorCodes.ORDER_BOOKING_NOT_FOUND.equals(error.getError())) {
                    // Бронирование заказа не найдено
                    throw new OrderBookingNotFoundException(error.getMessage());
                }

                // Неизвестная ошибка
                throw new WarehouseClientException("Неожиданная ошибка обработки ответа сервиса склада.", e);

            } catch (JsonProcessingException ex) {
                // Ошибка парсинга JSON
                throw new WarehouseClientException("Неожиданная ошибка ответа сервиса склада.", ex);
            }
        }
    }

    // ==================== FALLBACK МЕТОД (Circuit Breaker) ====================

    /**
     * Fallback для отгрузки товаров
     * Вызывается при недоступности сервиса склада
     */
    public void shipProductsToDeliveryFallback(ShippedToDeliveryRequest request, Throwable t) {
        throw new WarehouseServiceUnavailableException(
                ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}