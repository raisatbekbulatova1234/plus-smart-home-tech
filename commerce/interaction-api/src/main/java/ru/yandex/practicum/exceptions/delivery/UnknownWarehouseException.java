package ru.yandex.practicum.exceptions.delivery;

public class UnknownWarehouseException extends RuntimeException {
    public UnknownWarehouseException(String message) {
        super(message);
    }
}
