package ru.yandex.practicum.handler.hubs;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;

@Component
public class ScenarioRemovedEventHandler extends HubEventHandlerBase<ScenarioRemovedEventAvro> {

    public ScenarioRemovedEventHandler(KafkaCollectorProducer producer, HubEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    protected ScenarioRemovedEventAvro mapToHubEventAvroPayload(HubEventProto event) {
        return mapper.toScenarioRemovedEventAvroFromProto(event.getScenarioRemoved());
    }
}
