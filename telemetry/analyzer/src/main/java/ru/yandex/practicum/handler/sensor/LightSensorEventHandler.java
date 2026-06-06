package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
/**
 * Обработчик событий от датчика освещённости (LightSensor).
 *
 * Датчик освещённости измеряет один параметр:
 * - Яркость/освещённость (Luminosity) - в люксах (lux)
 *
 * Поддерживаемые типы условий:
 * - LUMINOSITY - проверка уровня освещённости
*/
@Component
public class LightSensorEventHandler extends SensorEventHandlerBase<LightSensorAvro> {

    public LightSensorEventHandler() {
        super(LightSensorAvro.class);
    }

    @Override
    protected Integer extractValue(ConditionTypeAvro type, LightSensorAvro data) {
        if (type != ConditionTypeAvro.LUMINOSITY) {
            throw new UnsupportedConditionTypeException("Тип условия " + type + " " +
                    "не поддерживается для LightSensor");
        }

        return data.getLuminosity();
    }
}