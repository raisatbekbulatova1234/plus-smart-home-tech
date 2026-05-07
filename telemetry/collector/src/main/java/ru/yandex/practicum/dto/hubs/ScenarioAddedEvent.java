package ru.yandex.practicum.dto.hubs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ScenarioAddedEvent extends HubEvent {
    @Size(min = 3, message = "Название сценария должно быть не менее трех символов.")
    private String name;

    @NotEmpty(message = "Условия сценария должны быть указаны.")
    private List<@Valid ScenarioCondition> conditions;

    @NotEmpty(message = "Действия сценария должны быть указаны.")
    private List<@Valid DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}