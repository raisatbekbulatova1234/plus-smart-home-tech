package ru.yandex.practicum.dto.hubs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAction {
    @NotBlank(message = "sensorId устройства должен быть указан.")
    private String sensorId;

    @NotNull(message = "Действие должно быть указано.")
    private ActionType type;

    private Integer value;
}