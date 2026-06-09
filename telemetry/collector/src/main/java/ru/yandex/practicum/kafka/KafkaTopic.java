package ru.yandex.practicum.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopic {
    SENSOR("sensor"),
    HUB("hub");

    private final String configKey;

    KafkaTopic(String configKey) {
        this.configKey = configKey;
    }
}
