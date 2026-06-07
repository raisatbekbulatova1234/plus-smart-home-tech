package ru.yandex.practicum.exceptions.warehouse;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException(String message) {
        super(message);
    }
}
