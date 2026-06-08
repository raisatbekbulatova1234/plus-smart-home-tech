package ru.yandex.practicum.exceptions.client;

public class DeliveryServiceUnavailableException extends RuntimeException {

    public DeliveryServiceUnavailableException(String message) {
        super(message);
    }

    public DeliveryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}