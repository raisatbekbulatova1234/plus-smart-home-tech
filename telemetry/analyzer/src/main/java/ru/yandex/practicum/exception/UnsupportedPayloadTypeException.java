package ru.yandex.practicum.exception;

public class UnsupportedPayloadTypeException extends RuntimeException {
    public UnsupportedPayloadTypeException(String message) {
        super(message);
    }
}
