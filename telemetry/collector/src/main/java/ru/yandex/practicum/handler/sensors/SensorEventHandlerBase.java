package ru.yandex.practicum.handler.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.exception.HandlerException;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.KafkaTopic;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.SensorEventMapper;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class SensorEventHandlerBase<T extends SpecificRecordBase> implements SensorEventHandler {
    protected final KafkaCollectorProducer producer;
    protected final SensorEventMapper mapper;

    protected abstract T mapToSensorEventAvroPayload(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        log.info("Обработка Sensor event: hubId={}, type={}", event.getHubId(), event.getPayloadCase());

        if (event.getPayloadCase() != getMessageType()) {
            throw new HandlerException(
                    "Обработчик " + getClass().getSimpleName() +
                            " не может обработать событие с типом: " + event.getPayloadCase()
            );
        }

        SensorEventAvro eventAvro = buildAvro(event);
        log.debug("Создан sensorAvro-объект: {}", eventAvro);
        producer.send(KafkaTopic.SENSOR, eventAvro.getTimestamp(), eventAvro.getHubId(), eventAvro);
    }

    private SensorEventAvro buildAvro(SensorEventProto event) {
        T payload = mapToSensorEventAvroPayload(event);

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}
