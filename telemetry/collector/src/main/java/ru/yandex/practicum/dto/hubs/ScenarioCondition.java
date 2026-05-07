package ru.yandex.practicum.dto.hubs;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    @NotBlank(message = "sensorId должен быть указан.")
    private String sensorId;

    @NotNull(message = "Тип сенсора должен быть указан.")
    private ConditionType type;

    @NotNull(message = "Операция должна быть указана.")
    private ConditionOperation operation;

    private Object value;

    @AssertTrue(message = "value должен быть Integer или Boolean.")
    public boolean isValueValid() {
        return value == null ||
                value instanceof Integer ||
                value instanceof Boolean;
    }
}