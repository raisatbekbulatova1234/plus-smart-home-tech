package ru.yandex.practicum.dto.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ClimateSensorEvent extends SensorEvent {
    @NotNull(message = "Температура должна быть указана.")
    private Integer temperatureC;

    @NotNull(message = "Влажность должна быть указана.")
    private Integer humidity;

    @NotNull(message = "Содержание углекислого газа должна быть указано.")
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}