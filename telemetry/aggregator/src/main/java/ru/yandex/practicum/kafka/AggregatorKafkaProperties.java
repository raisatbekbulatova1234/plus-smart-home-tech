package ru.yandex.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("aggregator.kafka")
public class AggregatorKafkaProperties {
    private ProducerConfiguration producer = new ProducerConfiguration();
    private ConsumerConfiguration consumer = new ConsumerConfiguration();

    @Getter
    @Setter
    public static class ProducerConfiguration {
        private Map<String, String> properties = new HashMap<>();
        private String snapshotTopic;
    }

    @Getter
    @Setter
    public static class ConsumerConfiguration {
        private Map<String, String> properties = new HashMap<>();
        private String sensorTopic;
        private Duration pollTimeout = Duration.ofMillis(100);
        private int batchSize;
    }
}


