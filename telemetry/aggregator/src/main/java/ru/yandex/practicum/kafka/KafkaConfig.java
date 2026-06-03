package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Bean
    public KafkaConsumer<String, SensorEventAvro> kafkaConsumer(KafkaProperties props) {

        Map<String, Object> config = new HashMap<>(props.getConsumer().getProperties());

        return new KafkaConsumer<String, SensorEventAvro>(config);
    }

    @Bean
    public KafkaProducer<String, SensorsSnapshotAvro> kafkaProducer(KafkaProperties props) {

        Map<String, Object> config = new HashMap<>(props.getProducer().getProperties());

        return new KafkaProducer<String, SensorsSnapshotAvro>(config);
    }
}