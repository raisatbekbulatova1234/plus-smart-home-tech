package ru.yandex.practicum.exception;

public class HubRouterSendException extends RuntimeException {
    public HubRouterSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
