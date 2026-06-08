package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.DeviceAlreadyExistsException;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {
    private final SensorRepository repository;

    public Sensor save(String hubId, String id) {

        if (repository.existsByIdInAndHubId(List.of(id), hubId)) {
            log.warn("Попытка зарегистрировать уже существующий сенсор: id={}, hubId={}", id, hubId);
            throw new DeviceAlreadyExistsException(
                    "Устройство с id " + id + " уже зарегистрировано в хабе " + hubId
            );
        }

        Sensor sensor = new Sensor();
        sensor.setId(id);
        sensor.setHubId(hubId);

        Sensor saved = repository.save(sensor);
        log.info("Сенсор зарегистрирован: id={}, hubId={}", id, hubId);
        return saved;
    }

    public void delete(String hubId, String id) {
        Optional<Sensor> sensorOpt = repository.findByIdAndHubId(id, hubId);

        if (sensorOpt.isPresent()) {
            repository.delete(sensorOpt.get());
            log.info("Сенсор удален: id={}, hubId={}", id, hubId);
        } else {
            log.debug("Сенсор для удаления не найден: id={}, hubId={}", id, hubId);
        }
    }
}
