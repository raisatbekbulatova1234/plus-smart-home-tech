package ru.yandex.practicum.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exceptions.cart.NoProductsInShoppingCartException;
import ru.yandex.practicum.exceptions.cart.NotAuthorizedUserException;
import ru.yandex.practicum.exceptions.cart.ShoppingCartNotFoundException;
import ru.yandex.practicum.exceptions.client.WarehouseServiceUnavailableException;
import ru.yandex.practicum.exceptions.handler.ErrorCodes;
import ru.yandex.practicum.exceptions.handler.ErrorResponse;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.ProductInShoppingCartLowQuantityInWarehouse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Глобальный обработчик ошибок для REST контроллеров корзины покупок
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

    // ==================== ОШИБКИ АВТОРИЗАЦИИ ====================

    /**
     * Обработка ошибки "Неавторизованный пользователь"
     * HTTP 401 - Unauthorized
     */
    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleNotAuthorizedUserException(
            NotAuthorizedUserException ex,
            HttpServletRequest request
    ) {
        log.warn("Имя пользователя не прошло проверку: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .error(ErrorCodes.NOT_AUTHORIZED_USER)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.NOT_AUTHORIZED_USER.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ КОРЗИНЫ ====================

    /**
     * Обработка ошибки "В корзине нет товаров"
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNoProductsInShoppingCartException(
            NoProductsInShoppingCartException ex,
            HttpServletRequest request
    ) {
        log.warn("Товары в корзине пользователя не найдены: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.PRODUCT_IN_CART_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.PRODUCT_IN_CART_NOT_FOUND.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Обработка ошибки "Корзина не найдена"
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(ShoppingCartNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleShoppingCartNotFoundException(
            ShoppingCartNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Корзина пользователя не найдена: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .error(ErrorCodes.SHOPPING_CART_NOT_FOUND)
                .message(ex.getMessage())
                .userMessage(ErrorCodes.SHOPPING_CART_NOT_FOUND.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== ОШИБКИ СКЛАДА ====================

    /**
     * Обработка ошибки "Товар не найден на складе"
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
     * Обработка ошибки "Недостаточно товара на складе"
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
     * Обработка ошибки "Сервис склада недоступен"
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