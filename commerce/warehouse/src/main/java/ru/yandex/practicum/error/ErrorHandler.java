package ru.yandex.practicum.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exceptions.warehouse.SpecifiedProductAlreadyInWarehouseException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Глобальный обработчик ошибок для REST контроллеров склада (warehouse)
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

    // ==================== ОШИБКИ СКЛАДА ====================

    /**
     * Товар не найден на складе
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNoSpecifiedProductException(
            NoSpecifiedProductInWarehouseException ex,
            HttpServletRequest request
    ) {
        log.warn("Товар на складе не найден: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.NO_PRODUCT_IN_WAREHOUSE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.NO_PRODUCT_IN_WAREHOUSE.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Недостаточно товара на складе
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleProductInShoppingCartLowQuantityException(
            ProductInShoppingCartLowQuantityInWarehouse ex,
            HttpServletRequest request
    ) {
        log.warn("Недостаточно товара на складе: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.LOW_QUANTITY_IN_WAREHOUSE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.LOW_QUANTITY_IN_WAREHOUSE.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Товар уже зарегистрирован на складе
     * Возникает при попытке добавить товар, который уже есть
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSpecifiedProductAlreadyInWarehouseException(
            SpecifiedProductAlreadyInWarehouseException ex,
            HttpServletRequest request
    ) {
        log.warn("Товар уже зарегистрирован на складе: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.PRODUCT_ALREADY_IN_WAREHOUSE)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.PRODUCT_ALREADY_IN_WAREHOUSE.getMessage())
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
                .message(ErrorCodes.INTERNAL_SERVER_ERROR.getMessage())
                .userMessage("На сервере произошла ошибка. Попробуйте позже.")
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