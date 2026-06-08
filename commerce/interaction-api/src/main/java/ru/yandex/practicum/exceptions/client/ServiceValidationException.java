package ru.yandex.practicum.exceptions.client;

import java.util.List;

public class ServiceValidationException extends RuntimeException {
    private final List<String> errors;

    public ServiceValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}