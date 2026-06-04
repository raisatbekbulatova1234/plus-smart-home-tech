package ru.yandex.practicum.handler.hub;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.exception.UnsupportedPayloadTypeException;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
/**
 * Абстрактный базовый класс для всех обработчиков событий хаба.
 *
 * Реализует паттерн "Шаблонный метод" (Template Method):
 * - handle() - общий алгоритм обработки (получение payload, валидация, логирование)
 * - process() - абстрактный метод, который реализуют конкретные обработчики
 *
 * Использует Generics для типобезопасной работы с разными типами payload.
 *
 * @param <T> - тип payload'а, который обрабатывает конкретный обработчик
 *            (DeviceAddedEventAvro, DeviceRemovedEventAvro, ScenarioAddedEventAvro и т.д.)
 */
@Slf4j
public abstract class HubEventHandlerBase<T> implements HubEventHandler {
    private final Class<T> payloadType;

    protected HubEventHandlerBase(Class<T> payloadType) {
        this.payloadType = payloadType;
    }

    @Override
    public Class<?> getPayloadType() {
        return payloadType;
    }

    @Override
    public void handle(HubEventAvro event) {
        Object payload = event.getPayload();

        if (!payloadType.isInstance(payload)) {
            throw new UnsupportedPayloadTypeException(
                    "Неверный тип payload: " + payload.getClass()
            );
        }

        T typedPayload = payloadType.cast(payload);

        log.info("Обработка события: hubId={}, type={}",
                event.getHubId(),
                payloadType.getSimpleName());

        process(event, typedPayload);
    }

    protected abstract void process(HubEventAvro event, T payload);
}