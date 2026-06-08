package ru.yandex.practicum.exceptions.client;

public class DeliveryClientException extends RuntimeException {

    public DeliveryClientException(String message) {
        super(message);
    }

    public DeliveryClientException(String message, Throwable cause) {
        super(message, cause);
    }
}