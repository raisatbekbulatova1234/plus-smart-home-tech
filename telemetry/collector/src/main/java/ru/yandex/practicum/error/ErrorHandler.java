package ru.yandex.practicum.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exception.KafkaConfigurationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        logValidationError(ex, errors);

        return new ApiError(
                "Ошибки валидации данных",
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now(),
                errors
        );
    }

    @ExceptionHandler(KafkaConfigurationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleKafkaConfigurationException(KafkaConfigurationException ex) {
        String errorMessage = "Внутренние ошибки в работе сервиса.";

        log.warn("Ошибка в параметрах настройки kafka: {} - {}",
                ex.getClass().getSimpleName(), ex.getMessage());

        return new ApiError(
                errorMessage,
                "Kafka Configuration Error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                LocalDateTime.now(),
                List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex) {
        String errorMessage = "Произошла ошибка на сервере.";
        log.error("Необработанное исключение: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return new ApiError(
                errorMessage,
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                LocalDateTime.now(),
                List.of(ex.getMessage())
        );
    }

    private String formatFieldError(FieldError error) {
        return String.format("Поле '%s': %s", error.getField(), error.getDefaultMessage());
    }

    private void logValidationError(MethodArgumentNotValidException ex, List<String> errors) {
        log.warn("Валидация не пройдена: {} ошибок в {} полях. Детали: {}",
                errors.size(),
                ex.getBindingResult().getErrorCount(),
                errors);
    }
}