package ru.yandex.practicum.handler.sensors;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.mapper.SensorEventMapper;

@Component
public class MotionSensorEventHandler extends SensorEventHandlerBase<MotionSensorAvro> {

    public MotionSensorEventHandler(KafkaCollectorProducer producer, SensorEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    protected MotionSensorAvro mapToSensorEventAvroPayload(SensorEventProto event) {
        return mapper.toMotionSensorEventAvroFromProto(event.getMotionSensor());
    }
}
