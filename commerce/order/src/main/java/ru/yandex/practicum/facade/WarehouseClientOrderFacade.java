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

/**
 * Фасад для взаимодействия с сервисом склада (Warehouse Service)
 *
 * Отвечает за:
 * - Бронирование товаров для заказа (assembleOrder)
 * - Возврат товаров на склад (returnProducts)
 * - Получение адреса склада (getAddress)
 *
 * Использует Circuit Breaker для защиты от сбоев сервиса склада
 */
@Service
@RequiredArgsConstructor
public class WarehouseClientOrderFacade {

    private final WarehouseClient warehouseClient;   // Feign клиент для вызова сервиса склада
    private final ObjectMapper objectMapper;

    // ==================== ВОЗВРАТ ТОВАРОВ ====================

    /**
     * Возврат товаров на склад (при отмене заказа или возврате)
     */
    @CircuitBreaker(name = "warehouseOrder", fallbackMethod = "returnProductsFallback")
    public void returnProducts(Map<UUID, Long> products) {
        try {
            warehouseClient.returnProducts(products);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    // ==================== СБОРКА ЗАКАЗА ====================

    /**
     * Бронирование товаров для заказа на складе
     * Проверяет наличие товаров, резервирует их и возвращает параметры доставки
     */
    @CircuitBreaker(name = "warehouseOrder", fallbackMethod = "assembleOrderFallback")
    public BookedProductsDto assembleOrder(AssemblyProductsForOrderRequest request) {
        try {
            return warehouseClient.assembleOrder(request);
        } catch (FeignException e) {
            throw handleFeignException(e);
        }
    }

    // ==================== АДРЕС СКЛАДА ====================

    /**
     * Получение адреса склада (откуда будет отправлен заказ)
     */
    @CircuitBreaker(name = "warehouseOrder", fallbackMethod = "getAddressFallback")
    public AddressDto getAddress() {
        try {
            return warehouseClient.getAddress();
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
                        // Ошибка валидации входных данных
                        yield new ServiceValidationException(error.getMessage(), error.getValidationErrors());
                    }
                    if (ErrorCodes.ORDER_BOOKING_ALREADY_EXIST.equals(error.getError())) {
                        // Бронирование для этого заказа уже существует
                        yield new OrderBookingAlreadyExistsException(error.getMessage());
                    }
                    // Другая ошибка клиента
                    yield new WarehouseClientException(error.getMessage(), e);
                }

                // HTTP 404 - Not Found
                case 404 -> {
                    if (ErrorCodes.NO_PRODUCT_IN_WAREHOUSE.equals(error.getError())) {
                        // Товар отсутствует на складе
                        yield new NoSpecifiedProductInWarehouseException(error.getMessage());
                    }
                    yield new WarehouseClientException(error.getMessage(), e);
                }

                // Другие HTTP статусы (500, 503 и т.д.)
                default -> new WarehouseClientException(
                        "Неожиданный ответ сервиса склада. HTTP " + e.status(), e
                );
            };

        } catch (JsonProcessingException ex) {
            // Не удалось разобрать JSON ответ
            return new WarehouseClientException(
                    "Ошибка разбора ответа сервиса склада.", ex
            );
        }
    }

    // ==================== FALLBACK МЕТОДЫ (Circuit Breaker) ====================

    /**
     * Fallback для возврата товаров
     * Вызывается при недоступности сервиса склада
     */
    public void returnProductsFallback(Map<UUID, Long> products, Throwable t) {
        throw warehouseUnavailable(t);
    }

    /**
     * Fallback для сборки заказа
     * Вызывается при недоступности сервиса склада
     */
    public BookedProductsDto assembleOrderFallback(AssemblyProductsForOrderRequest request, Throwable t) {
        throw warehouseUnavailable(t);
    }

    /**
     * Fallback для получения адреса склада
     * Вызывается при недоступности сервиса склада
     */
    public AddressDto getAddressFallback(Throwable t) {
        throw warehouseUnavailable(t);
    }

    /**
     * Создание исключения о недоступности сервиса склада
     *
     * @param t исходное исключение
     * @return WarehouseServiceUnavailableException
     */
    private WarehouseServiceUnavailableException warehouseUnavailable(Throwable t) {
        return new WarehouseServiceUnavailableException(
                ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage(), t);
    }
}