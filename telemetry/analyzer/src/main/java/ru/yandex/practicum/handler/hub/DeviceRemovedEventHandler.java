package ru.yandex.practicum.handler.hub;

import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.SensorService;

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
