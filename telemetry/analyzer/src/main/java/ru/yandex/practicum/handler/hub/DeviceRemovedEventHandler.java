package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.SensorService;

/**
 * Обработчик события "Устройство удалено" (DeviceRemovedEvent).
 *
 * Это событие приходит от хаба, когда от него отключается устройство (датчик).
 *
 * Логика обработки:
 * - Удаляет датчик из системы через SensorService
 * - Также должны быть удалены все сценарии, использующие этот датчик
 */
@Component
public class DeviceRemovedEventHandler extends HubEventHandlerBase<DeviceRemovedEventAvro> {
    private final SensorService service;

    public DeviceRemovedEventHandler(SensorService service) {
        super(DeviceRemovedEventAvro.class);
        this.service = service;
    }

    @Override
    protected void process(HubEventAvro event,
                           DeviceRemovedEventAvro payload) {
        service.delete(event.getHubId(), payload.getId());
    }
}