package ru.yandex.practicum.handler.sensors;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

import ru.yandex.practicum.mapper.SensorEventMapper;

@Component
public class ClimateSensorEventHandler extends SensorEventHandlerBase<ClimateSensorAvro> {

    public ClimateSensorEventHandler(KafkaCollectorProducer producer, SensorEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    protected ClimateSensorAvro mapToSensorEventAvroPayload(SensorEventProto event) {
        return mapper.toClimateSensorEventAvroFromProto(event.getClimateSensor());
    }
}
