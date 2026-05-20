package ru.yandex.practicum.dto.hubs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioRemovedEvent extends HubEvent {
    @Size(min = 3, message = "Название сценария должно быть не менее трех символов.")
    @NotBlank(message = "Имя сценария должно быть указано.")
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}