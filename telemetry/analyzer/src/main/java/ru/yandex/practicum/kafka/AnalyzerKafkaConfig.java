package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;
import java.util.Properties;

/**
 * Конфигурационный класс для настройки Kafka-консьюмеров в сервисе Analyzer.
 *
 * Отвечает за создание консьюмеров для чтения сообщений из двух топиков:
 * 1. HubEventAvro - события от хабов (добавление/удаление устройств, сценариев)
 * 2. SensorsSnapshotAvro - снапшоты состояния датчиков
 */
@Configuration
@RequiredArgsConstructor
public class AnalyzerKafkaConfig {


    private final AnalyzerKafkaProperties properties;  // Кастомные свойства Kafka

    /**
     * Создаёт Kafka-консьюмер для чтения событий от хабов.
     *
     * Типы сообщений:
     * - Ключ: String (например, hubId)
     * - Значение: HubEventAvro (DeviceAddedEvent, DeviceRemovedEvent, ScenarioAddedEvent, ScenarioRemovedEvent)
     *
     * @return настроенный KafkaConsumer для HubEventAvro
     */
    @Bean
    public KafkaConsumer<String, HubEventAvro> hubEventConsumer() {
        return new KafkaConsumer<>(
                buildProps(properties.getHubEventConfiguration().getProperties())
        );
    }

    /**
     * Создаёт Kafka-консьюмер для чтения снапшотов состояния датчиков.
     *
     * Типы сообщений:
     * - Ключ: String (например, hubId)
     * - Значение: SensorsSnapshotAvro (состояние всех датчиков хаба)
     *
     * @return настроенный KafkaConsumer для SensorsSnapshotAvro
     */
    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer() {
        return new KafkaConsumer<>(
                buildProps(properties.getSnapshotConfiguration().getProperties())
        );
    }

    /**
     * Преобразует Map<String, String> в Properties.
     *
     * @param source - карта с настройками (из AnalyzerKafkaProperties)
     * @return объект Properties для передачи в конструктор KafkaConsumer
     */
    private Properties buildProps(Map<String, String> source) {
        Properties props = new Properties();
        props.putAll(source);
        return props;
    }
}