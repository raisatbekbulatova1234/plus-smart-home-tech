package ru.yandex.practicum.exceptions.client;

public class ShoppingStoreServiceUnavailableException extends RuntimeException {

    public ShoppingStoreServiceUnavailableException(String message) {
        super(message);
    }

    public ShoppingStoreServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}