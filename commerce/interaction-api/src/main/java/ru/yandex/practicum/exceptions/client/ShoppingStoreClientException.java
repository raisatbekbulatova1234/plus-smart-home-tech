package ru.yandex.practicum.exceptions.client;

public class ShoppingStoreClientException extends RuntimeException {

    public ShoppingStoreClientException(String message) {
        super(message);
    }

    public ShoppingStoreClientException(String message, Throwable cause) {
        super(message, cause);
    }
}