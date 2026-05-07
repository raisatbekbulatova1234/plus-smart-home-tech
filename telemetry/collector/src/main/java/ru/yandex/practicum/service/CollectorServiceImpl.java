package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.hubs.*;
import ru.yandex.practicum.dto.sensors.*;
import ru.yandex.practicum.kafka.KafkaCollectorProducer;
import ru.yandex.practicum.kafka.KafkaTopic;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.mapper.SensorEventMapper;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;
    private final KafkaCollectorProducer producer;

    @Override
    public void processSensorEvent(SensorEvent event) {
        log.info("Обработка Sensor event: id={}, type={}", event.getId(), event.getType());
        SpecificRecordBase eventAvro = mapToSensorEventAvro(event);
        log.debug("Создан sensorAvro-объект: {}", eventAvro);
        producer.send(KafkaTopic.SENSOR, event.getTimestamp(), event.getHubId(), eventAvro);
    }

    @Override
    public void processHubEvent(HubEvent event) {
        log.info("Обработка Hub event: hubId={}, type={}", event.getHubId(), event.getType());
        SpecificRecordBase eventAvro = mapToHubEventAvro(event);
        log.debug("Создан hubAvro-объект: {}", eventAvro);
        producer.send(KafkaTopic.HUB, event.getTimestamp(), event.getHubId(), eventAvro);
    }

    private SpecificRecordBase mapToSensorEventAvro(SensorEvent event) {
        SpecificRecordBase payload = switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT -> sensorEventMapper
                    .toClimateSensorEventAvro((ClimateSensorEvent) event);
            case LIGHT_SENSOR_EVENT -> sensorEventMapper
                    .toLightSensorEventAvro((LightSensorEvent) event);
            case MOTION_SENSOR_EVENT -> sensorEventMapper
                    .toMotionSensorEventAvro((MotionSensorEvent) event);
            case SWITCH_SENSOR_EVENT -> sensorEventMapper
                    .toSwitchSensorEventAvro((SwitchSensorEvent) event);
            case TEMPERATURE_SENSOR_EVENT -> sensorEventMapper
                    .toTemperatureSensorEventAvro((TemperatureSensorEvent) event);
        };

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private SpecificRecordBase mapToHubEventAvro(HubEvent event) {
        SpecificRecordBase payload = switch (event.getType()) {
            case DEVICE_ADDED -> hubEventMapper
                    .toDeviceAddedEventAvro((DeviceAddedEvent) event);
            case DEVICE_REMOVED -> hubEventMapper
                    .toDeviceRemovedEventAvro((DeviceRemovedEvent) event);
            case SCENARIO_ADDED -> hubEventMapper
                    .toScenarioAddedEventAvro((ScenarioAddedEvent) event);
            case SCENARIO_REMOVED -> hubEventMapper
                    .toScenarioRemovedEventAvro((ScenarioRemovedEvent) event);
        };

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}