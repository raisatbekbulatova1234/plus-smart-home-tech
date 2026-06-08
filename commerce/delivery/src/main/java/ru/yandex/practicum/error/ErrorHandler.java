package ru.yandex.practicum.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exceptions.client.ServiceValidationException;
import ru.yandex.practicum.exceptions.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.OrderBookingNotFoundException;
import ru.yandex.practicum.exceptions.client.WarehouseServiceUnavailableException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Глобальный обработчик ошибок для REST контроллеров доставки (delivery)
 * Перехватывает исключения и возвращает структурированный JSON с ошибкой
 */
@Slf4j
@RestControllerAdvice           // AOP-перехват ошибок во всех контроллерах
public class ErrorHandler {

    // ==================== ОШИБКИ ВАЛИДАЦИИ ====================

    /**
     * Обработка ошибок валидации (@Valid)
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        // Собираем все ошибки валидации в читаемый формат
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
                .userMessage("Проверьте корректность заполнения полей")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(errors)
                .build();
    }

    /**
     * Обработка ошибок валидации при обращении к внешним сервисам
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

    // ==================== ОШИБКИ СКЛАДА ====================

    /**
     * Бронирование заказа на складе не найдено
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(OrderBookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderBookingNotFoundException(
            OrderBookingNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Бронь для заказа на складе не найдена: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.ORDER_BOOKING_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.ORDER_BOOKING_NOT_FOUND.getMessage())
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

    // ==================== ОБЩАЯ ОШИБКА (FALLBACK) ====================

    /**
     * Обработка всех непредвиденных ошибок
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
     * Форматирование ошибки валидации для конкретного поля
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