package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

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
