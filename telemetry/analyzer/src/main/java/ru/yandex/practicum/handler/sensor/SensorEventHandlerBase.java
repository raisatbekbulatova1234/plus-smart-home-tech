package ru.yandex.practicum.handler.sensor;

import ru.yandex.practicum.exception.UnsupportedPayloadTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public abstract class SensorEventHandlerBase<T> implements SensorEventHandler {
    private final Class<T> payloadType;

    protected SensorEventHandlerBase(Class<T> payloadType) {
        this.payloadType = payloadType;
    }

    @Override
    public Class<?> getPayloadType() {
        return payloadType;
    }

    @Override
    public Integer getValue(ConditionTypeAvro type, SensorStateAvro state) {
        Object data = state.getData();

        if (!payloadType.isInstance(data)) {
            throw new UnsupportedPayloadTypeException(
                    "Неверный тип sensor data: " + data.getClass()
            );
        }

        T typed = payloadType.cast(data);

        return extractValue(type, typed);
    }

    protected abstract Integer extractValue(ConditionTypeAvro type, T data);
}
