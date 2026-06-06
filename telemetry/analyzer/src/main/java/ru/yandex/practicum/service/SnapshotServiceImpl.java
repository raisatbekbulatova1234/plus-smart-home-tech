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

/**
 * Реализация сервиса обработки снапшотов состояния датчиков.
 * Основная логика:
 * 1. Получает снапшот (состояние всех датчиков хаба)
 * 2. Находит все сценарии хаба
 * 3. Проверяет, какие сценарии выполняются
 * 4. Отправляет команды на выполнение действий через gRPC
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {


    private final ScenarioRepository scenarioRepository;      // Поиск сценариев хаба
    private final List<SensorEventHandler> sensorEventHandlers; // Обработчики разных типов датчиков
    private final DeviceActionProtoMapper protoMapper;        // Преобразование Action → Protobuf
    private final HubRouterClient hubRouterClient;            // gRPC клиент для отправки команд

    /**
     * Обрабатывает снапшот состояния датчиков хаба.
     */
    @Transactional(readOnly = true)          // Только чтение, нет изменений в БД
    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        log.debug("Обработка снапшота: hubId={}, sensors={}", snapshot.getHubId(), snapshot.getSensorsState().size());

        // Блок 3.1: ПОИСК ВСЕХ СЦЕНАРИЕВ ХАБА
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

        // Блок 3.2: ФИЛЬТРАЦИЯ СЦЕНАРИЕВ, КОТОРЫЕ ВЫПОЛНЯЮТСЯ
        List<Scenario> matched = scenarios.stream().filter(s -> isScenarioMatched(s, snapshot.getSensorsState())).toList();

        log.debug("Найдено подходящих сценариев: hubId={}, count={}", snapshot.getHubId(), matched.size());

        // Блок 3.3: ВЫПОЛНЕНИЕ ПОДОШЕДШИХ СЦЕНАРИЕВ
        matched.forEach(s -> executeScenario(s, snapshot.getHubId()));
    }

    /**
     * Проверяет, выполняются ли все условия сценария на основе текущего состояния датчиков.
     * Все условия должны быть истинны (логическое И).
     */
    private boolean isScenarioMatched(Scenario scenario, Map<String, SensorStateAvro> stateMap) {
        // Проверяем, что ВСЕ условия сценария удовлетворены
        return scenario.getConditions().entrySet().stream().allMatch(entry -> {
            String sensorId = entry.getKey();          // ID датчика
            Condition condition = entry.getValue();    // Условие для проверки
            SensorStateAvro state = stateMap.get(sensorId); // Текущее состояние
            return isConditionSatisfied(condition, state);
        });
    }

    /**
     * Проверяет, удовлетворяет ли текущее состояние датчика заданному условию.
     */
    private boolean isConditionSatisfied(Condition condition, SensorStateAvro state) {

        if (state == null) {
            return false;
        }

        // Находим обработчик для типа датчика (Motion, Temperature, Light, etc.)
        SensorEventHandler handler = findHandlerForState(state);


        Integer actual = handler.getValue(condition.getType(), state);

        if (actual == null) {
            log.warn("SensorEventHandler вернул null: type={}, operation={}, expected={}, payloadType={}", condition.getType(), condition.getOperation(), condition.getValue(), state.getData().getClass().getSimpleName());
        }

        Integer expected = condition.getValue();
        ConditionOperationAvro operation = condition.getOperation();

        // Сравниваем фактическое значение с ожидаемым
        return compareCondition(actual, expected, operation);
    }

    /**
     * Выполняет действия сценария, отправляя команды на устройства через gRPC.
     */
    private void executeScenario(Scenario scenario, String hubId) {
        // Текущее время для метки запроса
        Instant ts = Instant.now();

        Timestamp timestamp = Timestamp.newBuilder().setSeconds(ts.getEpochSecond()).setNanos(ts.getNano()).build();

        // Для каждого действия в сценарии
        scenario.getActions().forEach((sensorId, action) -> {

            // Создаём gRPC-запрос
            DeviceActionRequest request = DeviceActionRequest.newBuilder().setHubId(hubId)                                    // Кому
                    .setScenarioName(scenario.getName())                // Имя сценария (для логов)
                    .setTimestamp(timestamp)                            // Время
                    .setAction(protoMapper.toProto(sensorId, action))   // Что делать
                    .build();

            // Отправляем запрос (с обработкой ошибок)
            safeSend(request, sensorId, scenario.getName());
        });
    }

    /**
     * Находит подходящий обработчик для состояния датчика.
     * Обработчики различаются по типу payload (MotionSensorProto, TemperatureSensorProto и т.д.)
     */
    private SensorEventHandler findHandlerForState(SensorStateAvro state) {
        Object data = state.getData();  // Получаем конкретный тип датчика

        return sensorEventHandlers.stream().filter(h -> h.getPayloadType().isInstance(data))  // Проверяем совместимость
                .findFirst().orElseThrow(() -> new UnsupportedPayloadTypeException("SensorEventHandler не поддерживает payload типа: " + data.getClass()));
    }

    /**
     * Сравнивает фактическое значение с ожидаемым согласно операции.
     */
    private boolean compareCondition(Integer actual, Integer expected, ConditionOperationAvro operation) {

        if (actual == null || expected == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actual.equals(expected);
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    /**
     * Отправляет gRPC-запрос в HubRouter с обработкой ошибок.
     */
    private void safeSend(DeviceActionRequest request, String sensorId, String scenarioName) {
        try {
            hubRouterClient.send(request);
        } catch (Exception ex) {
            String errorMessage = String.format("Отправка gRPC-сообщения провалилась: scenario=%s, sensorId=%s", scenarioName, sensorId);
            throw new HubRouterSendException(errorMessage, ex);
        }
    }
}