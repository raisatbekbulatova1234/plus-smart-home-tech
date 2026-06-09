package ru.yandex.practicum.handler.hubs;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;

@Component
public class ScenarioAddedEventHandler extends HubEventHandlerBase<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(KafkaCollectorProducer producer, HubEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToHubEventAvroPayload(HubEventProto event) {
        return mapper.toScenarioAddedEventAvroFromProto(event.getScenarioAdded());
    }
}
