package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
/**
 * Обработчик событий от климатического датчика (ClimateSensor).
 *
 * Климатический датчик измеряет три параметра:
 * - Температура (TemperatureC) - в градусах Цельсия
 * - Влажность (Humidity) - в процентах
 * - Уровень CO2 (Co2Level) - в ppm (parts per million)
 *
 * Поддерживаемые типы условий:
 * - TEMPERATURE - проверка температуры
 * - HUMIDITY - проверка влажности
 * - CO2LEVEL - проверка уровня углекислого газа
 */
@Component
public class ClimateSensorEventHandler extends SensorEventHandlerBase<ClimateSensorAvro> {

    public ClimateSensorEventHandler() {
        super(ClimateSensorAvro.class);
    }

    @Override
    protected Integer extractValue(ConditionTypeAvro type, ClimateSensorAvro data) {
        return switch (type) {
            case TEMPERATURE -> data.getTemperatureC();
            case HUMIDITY -> data.getHumidity();
            case CO2LEVEL -> data.getCo2Level();
            default -> throw new UnsupportedConditionTypeException("Тип условия " + type + " " +
                    "не поддерживается для ClimateSensor");
        };
    }
}