package ru.yandex.practicum.handler.sensors;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.mapper.SensorEventMapper;

public class TemperatureSensorEventHandler extends SensorEventHandlerBase<TemperatureSensorAvro> {

    public TemperatureSensorEventHandler(KafkaCollectorProducer producer, SensorEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    protected TemperatureSensorAvro mapToSensorEventAvroPayload(SensorEventProto event) {
        return mapper.toTemperatureSensorEventAvroFromProto(event.getTemperatureSensor());
    }
}
