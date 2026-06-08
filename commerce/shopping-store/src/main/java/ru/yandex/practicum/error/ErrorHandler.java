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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Глобальный обработчик ошибок для REST контроллеров магазина (shopping-store)
 * Перехватывает исключения и возвращает клиенту структурированный JSON с ошибкой
 */
@Slf4j
@RestControllerAdvice           // AOP-перехват ошибок во всех контроллерах
public class ErrorHandler {

    // ==================== ОШИБКИ ВАЛИДАЦИИ ====================

    /**
     * Обработка ошибок валидации (@Valid)
     * Выбрасывается когда DTO не проходит валидацию (например, @NotNull, @Size)
     *
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex,  // Исключение с деталями валидации
            HttpServletRequest request           // Запрос для получения пути URL
    ) {

        // Собираем все ошибки валидации в читаемый формат
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)      // Форматируем каждую ошибку поля
                .toList();

        logValidationError(ex, errors);           // Логируем ошибки

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error(ErrorCodes.VALIDATION_FAILED)
                .message(ErrorCodes.VALIDATION_FAILED.getMessage())
                .userMessage("Проверьте корректность заполнения полей.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(errors)          // Список ошибок по полям
                .build();
    }

    // ==================== ОШИБКИ ТОВАРОВ ====================

    /**
     * Обработка ошибки "Товар не найден"
     * Выбрасывается когда запрашиваемый товар отсутствует в БД
     *
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleProductNotFoundException(
            ProductNotFoundException ex,          // Исключение с информацией о товаре
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

    // ==================== ОБЩАЯ ОШИБКА (FALLBACK) ====================

    /**
     * Обработка всех непредвиденных ошибок (fallback handler)
     * Ловит любые исключения, которые не обработаны выше
     *
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Внутренняя ошибка сервера", ex);  // Полный stacktrace в лог

        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .error(ErrorCodes.INTERNAL_SERVER_ERROR)
                .message(ex.getClass().getName())    // Имя класса исключения
                .userMessage(ex.getMessage())       // ВАЖНО: для теста
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