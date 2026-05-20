package ru.yandex.practicum.dto.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MotionSensorEvent extends SensorEvent {
    @NotNull(message = "Качество соединения должно быть указано.")
    private Integer linkQuality;

    @NotNull(message = "Наличие движения должно быть указано.")
    private Boolean motion;

    @NotNull(message = "Напряжение должно быть указано.")
    private Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}