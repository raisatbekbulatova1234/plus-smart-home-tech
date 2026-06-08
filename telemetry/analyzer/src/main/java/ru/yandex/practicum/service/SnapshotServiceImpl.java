package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.HubRouterClient;
import ru.yandex.practicum.exception.HubRouterSendException;
import ru.yandex.practicum.exception.UnsupportedPayloadTypeException;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.handler.sensor.SensorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.mapper.DeviceActionProtoMapper;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
    private final ScenarioRepository scenarioRepository;
    private final List<SensorEventHandler> sensorEventHandlers;
    private final DeviceActionProtoMapper protoMapper;
    private final HubRouterClient hubRouterClient;

    @Transactional(readOnly = true)
    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        log.debug("Обработка снапшота: hubId={}, sensors={}",
                snapshot.getHubId(),
                snapshot.getSensorsState().size());

        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

        List<Scenario> matched = scenarios.stream()
                .filter(s -> isScenarioMatched(s, snapshot.getSensorsState()))
                .toList();

        log.debug("Найдено подходящих сценариев: hubId={}, count={}",
                snapshot.getHubId(),
                matched.size());

        matched.forEach(s -> executeScenario(s, snapshot.getHubId()));
    }

    private boolean isScenarioMatched(Scenario scenario,
                                      Map<String, SensorStateAvro> stateMap) {

        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> {

                    String sensorId = entry.getKey();
                    Condition condition = entry.getValue();
                    SensorStateAvro state = stateMap.get(sensorId);
                    return isConditionSatisfied(condition, state);
                });
    }

    private boolean isConditionSatisfied(Condition condition,
                                         SensorStateAvro state) {
        if (state == null) {
            return false;
        }

        SensorEventHandler handler = findHandlerForState(state);
        Integer actual = handler.getValue(condition.getType(), state);

        if (actual == null) {
            log.warn("SensorEventHandler вернул null: type={}, operation={}, expected={}, payloadType={}",
                    condition.getType(),
                    condition.getOperation(),
                    condition.getValue(),
                    state.getData().getClass().getSimpleName());
        }

        Integer expected = condition.getValue();
        ConditionOperationAvro operation = condition.getOperation();

        return compareCondition(actual, expected, operation);
    }

    private void executeScenario(Scenario scenario, String hubId) {
        Instant ts = Instant.now();

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(ts.getEpochSecond())
                .setNanos(ts.getNano())
                .build();

        scenario.getActions().forEach((sensorId, action) -> {

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenario.getName())
                    .setTimestamp(timestamp)
                    .setAction(protoMapper.toProto(sensorId, action))
                    .build();

            safeSend(request, sensorId, scenario.getName());
        });
    }

    private SensorEventHandler findHandlerForState(SensorStateAvro state) {
        Object data = state.getData();

        return sensorEventHandlers.stream()
                .filter(h -> h.getPayloadType().isInstance(data))
                .findFirst()
                .orElseThrow(() -> new UnsupportedPayloadTypeException(
                        "SensorEventHandler не поддерживает payload типа: " + data.getClass()
                ));
    }

    private boolean compareCondition(Integer actual,
                                     Integer expected,
                                     ConditionOperationAvro operation) {

        if (actual == null || expected == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actual.equals(expected);
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    private void safeSend(DeviceActionRequest request, String sensorId, String scenarioName) {
        try {
            hubRouterClient.send(request);
        } catch (Exception ex) {
            String errorMessage = String.format("Отправка gRPC-сообщения провалилась: scenario=%s, sensorId=%s",
                    scenarioName, sensorId);
            throw new HubRouterSendException(errorMessage, ex);
        }
    }
}
