package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class AnalyzerKafkaConfig {
    private final AnalyzerKafkaProperties properties;

    @Bean
    public KafkaConsumer<String, HubEventAvro> hubEventConsumer() {
        return new KafkaConsumer<>(
                buildProps(properties.getHubEventConfiguration().getProperties())
        );
    }

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer() {
        return new KafkaConsumer<>(
                buildProps(properties.getSnapshotConfiguration().getProperties())
        );
    }

    private Properties buildProps(Map<String, String> source) {
        Properties props = new Properties();
        props.putAll(source);
        return props;
    }
}
