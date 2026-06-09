package ru.yandex.practicum.handler.sensors;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.mapper.SensorEventMapper;

@Component
public class LightSensorEventHandler extends SensorEventHandlerBase<LightSensorAvro> {

    public LightSensorEventHandler(KafkaCollectorProducer producer, SensorEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    protected LightSensorAvro mapToSensorEventAvroPayload(SensorEventProto event) {
        return mapper.toLightSensorEventAvroFromProto(event.getLightSensor());
    }
}
