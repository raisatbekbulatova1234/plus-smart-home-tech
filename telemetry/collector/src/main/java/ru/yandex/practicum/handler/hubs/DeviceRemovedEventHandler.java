package ru.yandex.practicum.handler.hubs;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;

@Component
public class DeviceRemovedEventHandler extends HubEventHandlerBase<DeviceRemovedEventAvro> {

    public DeviceRemovedEventHandler(KafkaCollectorProducer producer, HubEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    protected DeviceRemovedEventAvro mapToHubEventAvroPayload(HubEventProto event) {
        return mapper.toDeviceRemovedEventAvroFromProto(event.getDeviceRemoved());
    }
}
