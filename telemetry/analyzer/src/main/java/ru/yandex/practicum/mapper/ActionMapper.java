package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.model.Action;
/**
 * Маппер для преобразования Avro-схемы DeviceActionAvro в JPA-сущность Action.
 * Отвечает за конвертацию данных из Kafka-сообщений в объекты для сохранения в БД.
 */
@Component
public class ActionMapper {

    public Action fromAvro(DeviceActionAvro avro) {
        Action action = new Action();

        action.setType(avro.getType());
        action.setValue(avro.getValue());

        return action;
    }
}