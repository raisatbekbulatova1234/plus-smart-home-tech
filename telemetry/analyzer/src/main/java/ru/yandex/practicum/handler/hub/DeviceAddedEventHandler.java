package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.SensorService;

/**
 * Обработчик события "Устройство добавлено" (DeviceAddedEvent).
 *
 * Это событие приходит от хаба, когда к нему подключается новое устройство (датчик).
 *
 * Логика обработки:
 * - Регистрирует новый датчик в системе через SensorService
 * - Сохраняет связь: датчик → хаб
 */
@Component
public class DeviceAddedEventHandler extends HubEventHandlerBase<DeviceAddedEventAvro> {

    private final SensorService service;

    public DeviceAddedEventHandler(SensorService service) {
        super(DeviceAddedEventAvro.class);
        this.service = service;
    }

    @Override
    protected void process(HubEventAvro event,
                           DeviceAddedEventAvro payload) {
        service.save(event.getHubId(), payload.getId());
    }
}