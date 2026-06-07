package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.repository.SnapshotRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {
    private final SnapshotRepository repository;

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {

        Optional<SensorsSnapshotAvro> existingSnapshot =
                repository.getSnapshotByHubId(event.getHubId());

        if (existingSnapshot.isEmpty()) {
            SensorsSnapshotAvro newSnapshot = createNewSnapshot(event);
            repository.addSnapshot(newSnapshot);
            return Optional.of(newSnapshot);
        }

        SensorsSnapshotAvro snapshot = existingSnapshot.get();

        if (isUpdateRequired(snapshot, event)) {
            updateSnapshot(snapshot, event);
        } else {
            return Optional.empty();
        }

        repository.addSnapshot(snapshot);
        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro createNewSnapshot (SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = SensorsSnapshotAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setHubId(event.getHubId())
                .setSensorsState(new HashMap<>())
                .build();

        snapshot.getSensorsState()
                .put(event.getId(), createState(event));

        return snapshot;
    }

    private SensorStateAvro createState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }

    private boolean isUpdateRequired(SensorsSnapshotAvro snapshot, SensorEventAvro event) {
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        String sensorId = event.getId();
        Instant newTimestamp = event.getTimestamp();

        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState == null) {
            return true;
        }

        if (newTimestamp.isBefore(oldState.getTimestamp())) {
            return false;
        }

        boolean isDataChanged = !Objects.equals(oldState.getData(), event.getPayload());
        return isDataChanged;
    }

    private void updateSnapshot(SensorsSnapshotAvro snapshot, SensorEventAvro event) {
        snapshot.getSensorsState()
                .put(event.getId(), createState(event));

        snapshot.setTimestamp(event.getTimestamp());
    }
}
