package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.model.Action;

/**
 * Маппер для преобразования JPA-сущности Action в gRPC-сообщение DeviceActionProto.
 * Используется для отправки команд на устройства через gRPC.
 * Направление: PostgreSQL → Kafka (Avro) → gRPC (Protobuf) → Устройство
 */
@Component
public class DeviceActionProtoMapper {

    /**
     * Преобразует JPA-сущность Action в gRPC-сообщение DeviceActionProto.
     *
     * @param sensorId - ID датчика/устройства, которому адресовано действие
     * @param action - JPA-сущность действия из базы данных
     * @return DeviceActionProto для отправки через gRPC на устройство
     */
    public DeviceActionProto toProto(String sensorId, Action action) {

        DeviceActionProto.Builder builder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(mapToActionTypeProto(action.getType()));


        if (action.getType() == ActionTypeAvro.SET_VALUE) {
            builder.setValue(action.getValue());
        }
        return builder.build();
    }

    /**
     * Преобразует тип действия из Avro-схемы в Protobuf-схему.
     *
     * @param type - тип действия в формате Avro
     * @return эквивалентный тип действия в формате Protobuf
     */
    private ActionTypeProto mapToActionTypeProto(ActionTypeAvro type) {
        return switch (type) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;      // Включить
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;  // Выключить
            case INVERSE -> ActionTypeProto.INVERSE;        // Инвертировать состояние
            case SET_VALUE -> ActionTypeProto.SET_VALUE;    // Установить значение
        };
    }
}