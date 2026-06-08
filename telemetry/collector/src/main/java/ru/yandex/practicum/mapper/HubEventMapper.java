package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Mapper(componentModel = "spring")
public interface HubEventMapper {
    DeviceAddedEventAvro toDeviceAddedEventAvroFromProto(DeviceAddedEventProto event);

    DeviceRemovedEventAvro toDeviceRemovedEventAvroFromProto(DeviceRemovedEventProto event);

    @Mapping(source = "conditionList", target = "conditions")
    @Mapping(source = "actionList", target = "actions")
    ScenarioAddedEventAvro toScenarioAddedEventAvroFromProto(ScenarioAddedEventProto event);

    ScenarioRemovedEventAvro toScenarioRemovedEventAvroFromProto(ScenarioRemovedEventProto event);

    @Mapping(target = "value", expression = "java(mapValue(condition))")
    ScenarioConditionAvro toScenarioConditionAvroFromProto(ScenarioConditionProto condition);

    @Mapping(target = "value", expression = "java(mapDeviceActionValue(action))")
    DeviceActionAvro toDeviceActionAvroFromProto(DeviceActionProto action);

    @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "UNRECOGNIZED")
    ActionTypeAvro toActionTypeAvroFromProto(ActionTypeProto type);

    @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "UNRECOGNIZED")
    DeviceTypeAvro toDeviceTypeAvroFromProto(DeviceTypeProto type);

    @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "UNRECOGNIZED")
    ConditionTypeAvro toConditionTypeAvroFromProto(ConditionTypeProto type);

    @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "UNRECOGNIZED")
    ConditionOperationAvro toConditionOperationAvroFromProto(ConditionOperationProto operation);

    default Object mapValue(ScenarioConditionProto proto) {
        return switch (proto.getValueCase()) {
            case BOOL_VALUE -> proto.getBoolValue();
            case INT_VALUE -> proto.getIntValue();
            case VALUE_NOT_SET -> null;
        };
    }

    default Integer mapDeviceActionValue(DeviceActionProto proto) {
        return proto.hasValue() ? proto.getValue() : null;
    }
}
