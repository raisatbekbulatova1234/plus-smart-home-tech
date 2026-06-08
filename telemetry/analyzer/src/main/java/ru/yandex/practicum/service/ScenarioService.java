package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.model.Scenario;

public interface ScenarioService {
    Scenario save(String hubId, ScenarioAddedEventAvro event);

    void delete(String hubId, String name);
}
