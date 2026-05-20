package ru.yandex.practicum.dto.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TemperatureSensorEvent extends SensorEvent {
    @NotNull(message = "Температура в градусах Цельсия должна быть указана.")
    private Integer temperatureC;

    @NotNull(message = "Температура в градусах Фаренгейта должна быть указана.")
    private Integer temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}