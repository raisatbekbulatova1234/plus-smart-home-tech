package ru.yandex.practicum.exceptions.cart;

public class CartValidationException extends RuntimeException {
    public CartValidationException(String message) {
        super(message);
    }
}
