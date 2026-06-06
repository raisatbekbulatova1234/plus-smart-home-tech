package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
/**
 * Обработчик событий от датчика температуры (TemperatureSensor).
 *
 * Датчик температуры измеряет один параметр:
 * - Температура (TemperatureC) - в градусах Цельсия
 *
 * Поддерживаемые типы условий:
 * - TEMPERATURE - проверка температуры
 *
 * Примечание: датчик также может предоставлять температуру в Фаренгейтах,
 * но в системе используется только Цельсий для унификации.
 */
@Component
public class TemperatureSensorEventHandler extends SensorEventHandlerBase<TemperatureSensorAvro> {

    public TemperatureSensorEventHandler() {
        super(TemperatureSensorAvro.class);
    }

    @Override
    protected Integer extractValue(ConditionTypeAvro type, TemperatureSensorAvro data) {
        if (type != ConditionTypeAvro.TEMPERATURE) {
            throw new UnsupportedConditionTypeException("Тип условия " + type + " " +
                    "не поддерживается для TemperatureSensor");
        }

        return data.getTemperatureC();
    }
}