package ru.yandex.practicum.dto.hubs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAddedEvent extends HubEvent {
    @NotBlank(message = "id устройства должен быть указан.")
    private String id;

    @NotNull(message = "Тип устройства должен быть указан.")
    private DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}