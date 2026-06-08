package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.model.Action;

@Component
public class ActionMapper {

    public Action fromAvro(DeviceActionAvro avro) {
        Action action = new Action();

        action.setType(avro.getType());
        action.setValue(avro.getValue());

        return action;
    }
}
