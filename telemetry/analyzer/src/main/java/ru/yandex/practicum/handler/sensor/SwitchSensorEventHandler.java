package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.UnsupportedConditionTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
/**
 * Обработчик событий от датчика-переключателя (SwitchSensor).
 *
 * Датчик-переключатель представляет собой бинарное устройство,
 * которое может находиться в одном из двух состояний:
 * - Включено (ON) / Выключено (OFF)
 * - Открыто (OPEN) / Закрыто (CLOSED)
 * - Активно (ACTIVE) / Неактивно (INACTIVE)
 *
 * Поддерживаемые типы условий:
 * - SWITCH - проверка состояния переключателя (true/false)
 *
 * Особенность: булево значение (true/false) преобразуется в Integer (1/0) для унификации хранения и сравнения в базе данных.
*/
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