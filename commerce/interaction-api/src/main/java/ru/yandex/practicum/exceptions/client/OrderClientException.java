package ru.yandex.practicum.exceptions.client;

public class OrderClientException extends RuntimeException {

    public OrderClientException(String message) {
        super(message);
    }

    public OrderClientException(String message, Throwable cause) {
        super(message, cause);
    }
}