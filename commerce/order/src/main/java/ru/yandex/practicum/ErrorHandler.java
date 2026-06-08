package ru.yandex.practicum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exceptions.client.*;
import ru.yandex.practicum.exceptions.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.order.NoOrderFoundException;
import ru.yandex.practicum.exceptions.order.OrderStateException;
import ru.yandex.practicum.exceptions.payment.PaymentNotFoundException;
import ru.yandex.practicum.exceptions.store.ProductNotFoundException;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.OrderBookingAlreadyExistsException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Глобальный обработчик ошибок для REST контроллеров заказов
 * Перехватывает исключения и возвращает структурированный JSON с ошибкой
 */
@Slf4j                          // Логирование
@RestControllerAdvice           // AOP-перехват ошибок во всех контроллерах
public class ErrorHandler {

    // ==================== ОШИБКИ ВАЛИДАЦИИ ====================

    /**
     * Ошибки валидации (@Valid)
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        logValidationError(ex, errors);

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.VALIDATION_FAILED)
                .message(ErrorCodes.VALIDATION_FAILED.getMessage())
                .userMessage("Проверьте корректность заполнения полей.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(errors)
                .build();
    }

    /**
     * Ошибки валидации при обращении к внешним сервисам
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(ServiceValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleServiceValidationException(
            ServiceValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Ошибки валидации при обращении к внешнему сервису: {}, {}", ex.getMessage(),
                ex.getErrors());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.VALIDATION_FAILED)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.VALIDATION_FAILED.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(ex.getErrors())
                .build();
    }

    // ==================== ОШИБКИ ЗАКАЗОВ ====================

    /**
     * Заказ не найден
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(NoOrderFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNoOrderFoundException(
            NoOrderFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Заказ не найден: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.ORDER_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.ORDER_NOT_FOUND.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Конфликт статуса заказа (недопустимая операция для текущего статуса)
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(OrderStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOrderStateException(
            OrderStateException ex,
            HttpServletRequest request
    ) {
        log.warn("Текущий статус заказа конфликтует с запросом: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.ORDER_STATE_CONFLICT)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.ORDER_STATE_CONFLICT.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Бронирование на складе уже существует
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(OrderBookingAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOrderBookingAlreadyExistsException(
            OrderBookingAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        log.warn("Бронирование в сервисе склада уже существует: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.ORDER_BOOKING_ALREADY_EXIST)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.ORDER_BOOKING_ALREADY_EXIST.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ СКЛАДА ====================

    /**
     * Товары не найдены на складе
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoSpecifiedProductInWarehouseException(
            NoSpecifiedProductInWarehouseException ex,
            HttpServletRequest request
    ) {
        log.warn("Продукты на складе не найдены: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.NO_PRODUCT_IN_WAREHOUSE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.NO_PRODUCT_IN_WAREHOUSE.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ ДОСТАВКИ ====================

    /**
     * Доставка не найдена
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(NoDeliveryFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoDeliveryFoundException(
            NoDeliveryFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Доставка не найдена: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.DELIVERY_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.DELIVERY_NOT_FOUND.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ ОПЛАТЫ ====================

    /**
     * Платеж не найден
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePaymentNotFoundException(
            PaymentNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Сведения об оплате не найдены: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.PAYMENT_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.PAYMENT_NOT_FOUND.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ МАГАЗИНА ====================

    /**
     * Товар не найден в магазине
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleProductNotFoundException(
            ProductNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Товар в магазине не найден: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.PRODUCT_IN_STORE_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.PRODUCT_IN_STORE_NOT_FOUND.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ ДОСТУПНОСТИ СЕРВИСОВ ====================

    /**
     * Сервис склада недоступен
     * HTTP 503 - Service Unavailable
     */
    @ExceptionHandler(WarehouseServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleWarehouseServiceUnavailableException(
            WarehouseServiceUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("Сервис склада недоступен: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .error(ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.WAREHOUSE_SERVICE_UNAVAILABLE.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Сервис доставки недоступен
     * HTTP 503 - Service Unavailable
     */
    @ExceptionHandler(DeliveryServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleDeliveryServiceUnavailableException(
            DeliveryServiceUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("Сервис службы доставки недоступен: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .error(ErrorCodes.DELIVERY_SERVICE_UNAVAILABLE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.DELIVERY_SERVICE_UNAVAILABLE.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Сервис платежей недоступен
     * HTTP 503 - Service Unavailable
     */
    @ExceptionHandler(PaymentServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handlePaymentServiceUnavailableException(
            PaymentServiceUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("Сервис службы платежей недоступен: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .error(ErrorCodes.PAYMENT_SERVICE_UNAVAILABLE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.PAYMENT_SERVICE_UNAVAILABLE.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОБЩАЯ ОШИБКА (FALLBACK) ====================

    /**
     * Непредвиденные ошибки
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Внутренняя ошибка сервера", ex);

        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .error(ErrorCodes.INTERNAL_SERVER_ERROR)
                .message(ex.getClass().getName())
                .userMessage(ex.getMessage()) // ВАЖНО: для теста
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Форматирование ошибки валидации
     */
    private String formatFieldError(FieldError error) {
        return String.format("Поле '%s': %s", error.getField(), error.getDefaultMessage());
    }

    /**
     * Логирование ошибок валидации
     */
    private void logValidationError(MethodArgumentNotValidException ex, List<String> errors) {
        log.warn("Валидация не пройдена: {} ошибок в {} полях. Детали: {}",
                errors.size(),
                ex.getBindingResult().getErrorCount(),
                errors);
    }
}