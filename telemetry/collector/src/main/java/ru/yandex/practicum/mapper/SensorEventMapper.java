package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Mapper(componentModel = "spring")
public interface SensorEventMapper {
    ClimateSensorAvro toClimateSensorEventAvroFromProto(ClimateSensorProto event);

    LightSensorAvro toLightSensorEventAvroFromProto(LightSensorProto event);

    MotionSensorAvro toMotionSensorEventAvroFromProto(MotionSensorProto event);

    SwitchSensorAvro toSwitchSensorEventAvroFromProto(SwitchSensorProto event);

    TemperatureSensorAvro toTemperatureSensorEventAvroFromProto(TemperatureSensorProto event);
}
