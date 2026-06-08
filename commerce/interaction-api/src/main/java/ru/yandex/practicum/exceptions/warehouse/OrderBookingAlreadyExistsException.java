package ru.yandex.practicum.exceptions.warehouse;

public class OrderBookingAlreadyExistsException extends RuntimeException {
    public OrderBookingAlreadyExistsException(String message) {
        super(message);
    }
}
