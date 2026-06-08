package ru.yandex.practicum.exceptions.client;

public class WarehouseServiceUnavailableException extends RuntimeException {

    public WarehouseServiceUnavailableException(String message) {
        super(message);
    }

    public WarehouseServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}