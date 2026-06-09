package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Sensor;

public interface SensorService {
    Sensor save(String hubId, String id);

    void delete(String hubId, String id);
}
