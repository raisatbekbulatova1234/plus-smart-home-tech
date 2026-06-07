package ru.yandex.practicum.repository;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface SnapshotRepository {
    Optional<SensorsSnapshotAvro> getSnapshotByHubId(String hubId);

    SensorsSnapshotAvro addSnapshot(SensorsSnapshotAvro snapshot);
}
