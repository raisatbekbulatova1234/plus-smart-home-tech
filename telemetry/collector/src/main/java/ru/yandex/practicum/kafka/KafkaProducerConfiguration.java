package ru.yandex.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties("collector.kafka")
public class KafkaProducerConfiguration {
    private ProducerConfiguration configuration = new ProducerConfiguration();

    @Getter
    @Setter
    @ToString
    public static class ProducerConfiguration {
        private Map<String, String> properties = new HashMap<>();
        private Map<String, String> topics = new HashMap<>();
    }
}
