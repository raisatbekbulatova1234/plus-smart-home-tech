package ru.yandex.practicum.exception;

public class KafkaConfigurationException extends RuntimeException {
    public KafkaConfigurationException(String message) {
        super(message);
    }
}
