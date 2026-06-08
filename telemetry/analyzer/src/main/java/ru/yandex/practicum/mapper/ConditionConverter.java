package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.ConditionValueTypeMismatchException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.Condition;

@Component
public class ConditionConverter {

    public Condition fromAvro(ScenarioConditionAvro avro) {
        Condition condition = new Condition();

        condition.setType(avro.getType());
        condition.setOperation(avro.getOperation());
        applyValue(condition, avro.getValue());

        return condition;
    }

    public Boolean getBooleanValue(Condition condition) {
        if (condition.getValue() == null) {
            return null;
        }

        if (!isBooleanType(condition.getType())) {
            throwTypeMismatch(condition.getType(), condition.getValue(), "BOOLEAN");
        }

        return condition.getValue() == 1;
    }

    public Integer getIntValue(Condition condition) {
        if (condition.getValue() == null) {
            return null;
        }

        if (isBooleanType(condition.getType())) {
            throwTypeMismatch(condition.getType(), condition.getValue(), "INTEGER");
        }

        return condition.getValue();
    }

    private void applyValue(Condition condition, Object avroValue) {
        if (avroValue == null) {
            condition.setValue(null);
            return;
        }

        if (isBooleanType(condition.getType())) {
            condition.setValue(toBooleanValue(condition.getType(), avroValue));
        } else {
            condition.setValue(toIntegerValue(condition.getType(), avroValue));
        }
    }

    private boolean isBooleanType(ConditionTypeAvro type) {
        if (type == null) {
            return false;
        }

        return switch (type) {
            case MOTION, SWITCH -> true;
            case TEMPERATURE, HUMIDITY, CO2LEVEL, LUMINOSITY -> false;
        };
    }

    private Integer toBooleanValue(ConditionTypeAvro type, Object avroValue) {
        if (!(avroValue instanceof Boolean)) {
            throwTypeMismatch(type, avroValue, "BOOLEAN");
        }

        return (Boolean) avroValue ? 1 : 0;
    }

    private Integer toIntegerValue(ConditionTypeAvro type, Object avroValue) {
        if (!(avroValue instanceof Integer)) {
            throwTypeMismatch(type, avroValue, "INTEGER");
        }

        return (Integer) avroValue;
    }

    private void throwTypeMismatch(ConditionTypeAvro type, Object value, String expected) {
        throw new ConditionValueTypeMismatchException(
                "Для типа условия " + type +
                        " ожидалось значение типа " + expected +
                        ", но получено: " + value.getClass().getSimpleName()
        );
    }
}
