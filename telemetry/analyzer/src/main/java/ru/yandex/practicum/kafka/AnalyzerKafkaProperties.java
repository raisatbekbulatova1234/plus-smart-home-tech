package ru.yandex.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("analyzer.kafka")
public class AnalyzerKafkaProperties {
    private ConsumerConfiguration hubEventConfiguration;
    private ConsumerConfiguration snapshotConfiguration;

    @Getter
    @Setter
    public static class ConsumerConfiguration {
        private Map<String, String> properties = new HashMap<>();
        private String topic;
        private Duration pollTimeout = Duration.ofMillis(100);
        private int batchSize;
    }
}
