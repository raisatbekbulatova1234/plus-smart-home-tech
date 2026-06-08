package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

@Component
public class MotionSensorEventHandler extends SensorEventHandlerBase<MotionSensorAvro> {

    public MotionSensorEventHandler() {
        super(MotionSensorAvro.class);
    }

    @Override
    protected Integer extractValue(ConditionTypeAvro type, MotionSensorAvro data) {
        if (type != ConditionTypeAvro.MOTION) {
            throw new UnsupportedConditionTypeException("Тип условия " + type + " " +
                    "не поддерживается для MotionSensor");
        }

        return data.getMotion() ? 1 : 0;
    }
}
