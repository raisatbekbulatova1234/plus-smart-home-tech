package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.sensors.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Mapper(componentModel = "spring")
public interface SensorEventMapper {
    ClimateSensorAvro toClimateSensorEventAvro(ClimateSensorEvent event);

    LightSensorAvro toLightSensorEventAvro(LightSensorEvent event);

    MotionSensorAvro toMotionSensorEventAvro(MotionSensorEvent event);

    SwitchSensorAvro toSwitchSensorEventAvro(SwitchSensorEvent event);

    TemperatureSensorAvro toTemperatureSensorEventAvro(TemperatureSensorEvent event);
}