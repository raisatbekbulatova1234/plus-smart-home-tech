package ru.yandex.practicum.dto.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LightSensorEvent extends SensorEvent {
    @NotNull(message = "Качество соединения должно быть указано.")
    private Integer linkQuality;

    @NotNull(message = "Освещенность должна быть указана.")
    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}