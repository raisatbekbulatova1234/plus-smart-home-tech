package ru.yandex.practicum.exceptions.client;

public class WarehouseClientException extends RuntimeException {

    public WarehouseClientException(String message) {
        super(message);
    }

    public WarehouseClientException(String message, Throwable cause) {
        super(message, cause);
    }
}