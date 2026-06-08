package ru.yandex.practicum.handler.sensors;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.mapper.SensorEventMapper;

@Component
public class SwitchSensorEventHandler extends SensorEventHandlerBase<SwitchSensorAvro> {

    public SwitchSensorEventHandler(KafkaCollectorProducer producer, SensorEventMapper mapper) {
        super(producer, mapper);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    protected SwitchSensorAvro mapToSensorEventAvroPayload(SensorEventProto event) {
        return mapper.toSwitchSensorEventAvroFromProto(event.getSwitchSensor());
    }
}
