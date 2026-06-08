package ru.yandex.practicum.exceptions.client;

public class PaymentClientException extends RuntimeException {

    public PaymentClientException(String message) {
        super(message);
    }

    public PaymentClientException(String message, Throwable cause) {
        super(message, cause);
    }
}