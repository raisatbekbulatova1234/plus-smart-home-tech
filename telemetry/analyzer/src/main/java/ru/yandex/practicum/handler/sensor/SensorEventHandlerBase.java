package ru.yandex.practicum.handler.sensor;

import ru.yandex.practicum.exception.UnsupportedPayloadTypeException;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

/**
 * Абстрактный базовый класс для всех обработчиков датчиков.
 * <p>
 * Реализует паттерн "Шаблонный метод" (Template Method):
 * - getValue() - общий алгоритм получения значения (извлечение данных, валидация типа)
 * - extractValue() - абстрактный метод для конкретной логики извлечения значения
 * <p>
 * Использует Generics для типобезопасной работы с разными типами датчиков:
 * - MotionSensorAvro, TemperatureSensorAvro, LightSensorAvro,
 * ClimateSensorAvro, SwitchSensorAvro
 *
 * @param <T> - тип данных датчика (например, MotionSensorAvro, TemperatureSensorAvro)
 */
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
        // Шаг 1: Извлекаем данные из состояния датчика
        Object data = state.getData();

        // Шаг 2: Проверяем, что данные имеют ожидаемый тип
        if (!payloadType.isInstance(data)) {
            // Если тип не совпадает - выбрасываем исключение
            throw new UnsupportedPayloadTypeException(
                    "Неверный тип sensor data: " + data.getClass()
            );
        }

        // Шаг 3: Безопасное приведение к нужному типу
        T typed = payloadType.cast(data);

        // Шаг 4: Вызываем конкретную реализацию извлечения значения
        return extractValue(type, typed);
    }

    //Абстрактный метод, который реализуют конкретные обработчики.
    //Содержит специфическую логику извлечения числового значения из данных конкретного типа датчика.
    protected abstract Integer extractValue(ConditionTypeAvro type, T data);
}