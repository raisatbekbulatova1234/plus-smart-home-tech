package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.DeviceNotFoundException;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.mapper.ActionMapper;
import ru.yandex.practicum.mapper.ConditionConverter;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioServiceImpl implements ScenarioService {
    private final ActionRepository actionRepository;
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;

    private final ConditionConverter conditionConverter;
    private final ActionMapper actionMapper;

    @Override
    @Transactional
    public Scenario save(String hubId, ScenarioAddedEventAvro event) {
        String name = event.getName();

        log.info("Запрос на сохранение/обновление сценария: hubId={}, name={}", hubId, name);

        return scenarioRepository.findByHubIdAndName(hubId, name)
                .map(s -> updateScenario(s, event))
                .orElseGet(() -> createScenario(hubId, event));
    }

    @Override
    @Transactional
    public void delete(String hubId, String name) {
        log.info("Удаление сценария: hubId={}, name={}", hubId, name);

        scenarioRepository.findByHubIdAndName(hubId, name)
                .ifPresent(scenario -> {
                    actionRepository.deleteAll(scenario.getActions().values());
                    conditionRepository.deleteAll(scenario.getConditions().values());
                    scenarioRepository.delete(scenario);
                    log.info("Сценарий удален: id={}", scenario.getId());
                });
    }

    private Scenario createScenario(String hubId, ScenarioAddedEventAvro event) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(event.getName());

        Map<String, Sensor> sensors = loadSensorsToMap(event);

        scenario.getConditions().putAll(buildConditions(event, sensors));
        scenario.getActions().putAll(buildActions(event, sensors));

        Scenario saved = scenarioRepository.save(scenario);
        log.info("Сценарий создан: id={}, hubId={}, name={}",
                saved.getId(), hubId, saved.getName());
        return saved;
    }

    private Scenario updateScenario(Scenario scenario, ScenarioAddedEventAvro event) {
        scenario.getConditions().clear();
        scenario.getActions().clear();

        Map<String, Sensor> sensors = loadSensorsToMap(event);

        scenario.getConditions().putAll(buildConditions(event, sensors));
        scenario.getActions().putAll(buildActions(event, sensors));

        Scenario saved = scenarioRepository.save(scenario);
        log.info("Сценарий обновлен: id={}, hubId={}, name={}",
                saved.getId(), saved.getHubId(), saved.getName());
        return saved;
    }

    private Map<String, Condition> buildConditions(ScenarioAddedEventAvro event, Map<String, Sensor> sensors) {
        Map<String, Condition> conditions = new HashMap<>();

        for (ScenarioConditionAvro c : event.getConditions()) {
            Sensor sensor = sensors.get(c.getSensorId());
            Condition condition = conditionConverter.fromAvro(c);
            conditions.put(sensor.getId(), condition);
        }

        return conditions;
    }

    private Map<String, Action> buildActions(ScenarioAddedEventAvro event, Map<String, Sensor> sensors) {
        Map<String, Action> actions = new HashMap<>();

        for (DeviceActionAvro a : event.getActions()) {
            Sensor sensor = sensors.get(a.getSensorId());
            Action action = actionMapper.fromAvro(a);
            actions.put(sensor.getId(), action);
        }

        return actions;
    }

    private Map<String, Sensor> loadSensorsToMap(ScenarioAddedEventAvro event) {
        Set<String> sensorIds = new HashSet<>();

        for (ScenarioConditionAvro c : event.getConditions()) {
            sensorIds.add(c.getSensorId());
        }

        for (DeviceActionAvro a : event.getActions()) {
            sensorIds.add(a.getSensorId());
        }

        List<Sensor> sensors = sensorRepository.findAllById(sensorIds);

        Map<String, Sensor> sensorMap = sensors.stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));

        validateAllSensorsFound(sensorMap, sensorIds);

        return sensorMap;
    }

    private void validateAllSensorsFound(Map<String, Sensor> sensorMap, Set<String> sensorIds) {
        if (sensorMap.size() != sensorIds.size()) {
            Set<String> found = sensorMap.keySet();
            Set<String> missing = new HashSet<>(sensorIds);
            missing.removeAll(found);

            log.warn("Не пройдена валидация сенсоров. найдены={}, отсутствуют={}", found, missing);

            throw new DeviceNotFoundException("Сенсоры не найдены: " + missing);
        }
    }
}
