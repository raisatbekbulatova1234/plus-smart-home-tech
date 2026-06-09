package ru.yandex.practicum.handler.sensor;

import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public interface SensorEventHandler {
    Class<?> getPayloadType();

    Integer getValue(ConditionTypeAvro conditionType, SensorStateAvro sensorState);
}
