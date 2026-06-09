package ru.yandex.practicum.exception;

public class ConditionValueTypeMismatchException extends RuntimeException {
    public ConditionValueTypeMismatchException(String message) {
        super(message);
    }
}
