package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.ConditionValueTypeMismatchException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.Condition;

/**
 * Конвертер для преобразования Avro-схемы ScenarioConditionAvro в JPA-сущность Condition.
 * Особенность: разные типы условий используют разные типы значений:
 * - BOOLEAN (MOTION, SWITCH) → значение хранится как 0/1
 * - INTEGER (TEMPERATURE, HUMIDITY, CO2LEVEL, LUMINOSITY) → значение хранится как int
 */
@Component
public class ConditionConverter {

    /**
     * Преобразует Avro-объект условия в JPA-сущность Condition.
     *
     * @param avro - объект ScenarioConditionAvro из Kafka-сообщения
     * @return JPA-сущность Condition для сохранения в базу данных
     */
    public Condition fromAvro(ScenarioConditionAvro avro) {
        Condition condition = new Condition();

        // Маппинг типа условия (MOTION, LUMINOSITY, TEMPERATURE и т.д.)
        condition.setType(avro.getType());

        // Маппинг операции сравнения (EQUALS, GREATER_THAN, LOWER_THAN)
        condition.setOperation(avro.getOperation());

        // Маппинг значения с учётом типа (boolean → 0/1, int → int)
        applyValue(condition, avro.getValue());

        return condition;
    }

    /**
     * Возвращает значение условия как Boolean.
     * Преобразует хранимый Integer (0/1) обратно в Boolean.
     *
     * @param condition - сущность условия
     * @return Boolean-значение (true/false) или null
     * @throws ConditionValueTypeMismatchException - если тип условия не поддерживает Boolean
     */
    public Boolean getBooleanValue(Condition condition) {
        if (condition.getValue() == null) {
            return null;
        }

        // Проверка, что тип условия действительно поддерживает Boolean
        if (!isBooleanType(condition.getType())) {
            throwTypeMismatch(condition.getType(), condition.getValue(), "BOOLEAN");
        }

        // Преобразование: 1 → true, 0 → false
        return condition.getValue() == 1;
    }

    /**
     * Возвращает значение условия как Integer.
     *
     * @param condition - сущность условия
     * @return Integer-значение или null
     * @throws ConditionValueTypeMismatchException - если тип условия не поддерживает Integer
     */
    public Integer getIntValue(Condition condition) {
        if (condition.getValue() == null) {
            return null;
        }

        // Проверка, что тип условия не является Boolean-типом
        if (isBooleanType(condition.getType())) {
            throwTypeMismatch(condition.getType(), condition.getValue(), "INTEGER");
        }

        return condition.getValue();
    }

    /**
     * Применяет значение к условию с учётом его типа.
     * Boolean-значения конвертируются в 0/1, Integer-значения сохраняются как есть.
     */
    private void applyValue(Condition condition, Object avroValue) {
        if (avroValue == null) {
            condition.setValue(null);
            return;
        }

        if (isBooleanType(condition.getType())) {
            // Boolean-типы (MOTION, SWITCH) → 0 или 1
            condition.setValue(toBooleanValue(condition.getType(), avroValue));
        } else {
            // Integer-типы (TEMPERATURE, HUMIDITY, CO2LEVEL, LUMINOSITY) → int
            condition.setValue(toIntegerValue(condition.getType(), avroValue));
        }
    }

    /**
     * Определяет, должен ли тип условия использовать Boolean-значение.
     *
     * @return true - для MOTION, SWITCH (логические состояния)
     *         false - для TEMPERATURE, HUMIDITY, CO2LEVEL, LUMINOSITY (числовые значения)
     */
    private boolean isBooleanType(ConditionTypeAvro type) {
        if (type == null) {
            return false;
        }

        return switch (type) {
            case MOTION, SWITCH -> true;
            case TEMPERATURE, HUMIDITY, CO2LEVEL, LUMINOSITY -> false;
        };
    }

    /**
     * Преобразует Boolean-значение в Integer (true→1, false→0).
     */
    private Integer toBooleanValue(ConditionTypeAvro type, Object avroValue) {
        if (!(avroValue instanceof Boolean)) {
            throwTypeMismatch(type, avroValue, "BOOLEAN");
        }

        return (Boolean) avroValue ? 1 : 0;
    }

    /**
     * Проверяет, что значение является Integer, и возвращает его.
     */
    private Integer toIntegerValue(ConditionTypeAvro type, Object avroValue) {
        if (!(avroValue instanceof Integer)) {
            throwTypeMismatch(type, avroValue, "INTEGER");
        }

        return (Integer) avroValue;
    }

    /**
     * Выбрасывает исключение при несоответствии типа значения ожидаемому.
     */
    private void throwTypeMismatch(ConditionTypeAvro type, Object value, String expected) {
        throw new ConditionValueTypeMismatchException(
                "Для типа условия " + type +
                        " ожидалось значение типа " + expected +
                        ", но получено: " + value.getClass().getSimpleName()
        );
    }
}