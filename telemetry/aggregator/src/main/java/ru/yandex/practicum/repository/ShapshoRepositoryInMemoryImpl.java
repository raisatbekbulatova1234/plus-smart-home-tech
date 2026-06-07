package ru.yandex.practicum.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ShapshoRepositoryInMemoryImpl implements SnapshotRepository {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> getSnapshotByHubId(String hubId) {
        return Optional.ofNullable(snapshots.get(hubId));
    }

    @Override
    public SensorsSnapshotAvro addSnapshot(SensorsSnapshotAvro snapshot) {
        snapshots.put(snapshot.getHubId(), snapshot);
        return snapshot;
    }
}
