package ru.yandex.practicum.handler.hubs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.exception.HandlerException;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.KafkaTopic;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class HubEventHandlerBase<T extends SpecificRecordBase> implements HubEventHandler {
    protected final KafkaCollectorProducer producer;
    protected final HubEventMapper mapper;

    protected abstract T mapToHubEventAvroPayload(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        log.info("Обработка Hub event: hubId={}, type={}", event.getHubId(), event.getPayloadCase());

        if (event.getPayloadCase() != getMessageType()) {
            throw new HandlerException(
                    "Обработчик " + getClass().getSimpleName() +
                            " не может обработать событие с типом: " + event.getPayloadCase()
            );
        }

        HubEventAvro eventAvro = buildAvro(event);
        log.debug("Создан sensorAvro-объект: {}", eventAvro);
        producer.send(KafkaTopic.HUB, eventAvro.getTimestamp(), eventAvro.getHubId(), eventAvro);
    }

    private HubEventAvro buildAvro(HubEventProto event) {
        T payload = mapToHubEventAvroPayload(event);

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}
