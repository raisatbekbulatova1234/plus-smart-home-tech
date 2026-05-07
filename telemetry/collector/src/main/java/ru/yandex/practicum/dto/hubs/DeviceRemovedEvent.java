package ru.yandex.practicum.dto.hubs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceRemovedEvent extends HubEvent {
    @NotBlank(message = "id устройства должен быть указан.")
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }
}