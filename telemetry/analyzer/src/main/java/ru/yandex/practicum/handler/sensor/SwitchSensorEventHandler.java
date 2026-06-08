package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Component
public class SwitchSensorEventHandler extends SensorEventHandlerBase<SwitchSensorAvro> {

    public SwitchSensorEventHandler() {
        super(SwitchSensorAvro.class);
    }

    @Override
    protected Integer extractValue(ConditionTypeAvro type, SwitchSensorAvro data) {
        if (type != ConditionTypeAvro.SWITCH) {
            throw new UnsupportedConditionTypeException("Тип условия " + type + " " +
                    "не поддерживается для SwitchSensor");
        }

        return data.getState() ? 1 : 0;
    }
}
