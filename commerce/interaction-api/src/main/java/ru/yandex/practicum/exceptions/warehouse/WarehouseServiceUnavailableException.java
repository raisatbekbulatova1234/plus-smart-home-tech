package ru.yandex.practicum.exceptions.warehouse;

public class WarehouseServiceUnavailableException
        extends RuntimeException {

    public WarehouseServiceUnavailableException(String message) {
        super(message);
    }

    public WarehouseServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
