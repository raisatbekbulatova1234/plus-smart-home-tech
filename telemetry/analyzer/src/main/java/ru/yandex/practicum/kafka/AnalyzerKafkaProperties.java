package ru.yandex.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
/**
Класс для привязки (binding) Kafka-конфигурации из файлов application.yml.
 */
@Getter
@Setter
@ConfigurationProperties("analyzer.kafka")
public class AnalyzerKafkaProperties {
    private ConsumerConfiguration hubEventConfiguration;//Конфигурация для консьюмера событий от хабов
    private ConsumerConfiguration snapshotConfiguration;//Конфигурация для консьюмера снапшотов датчиков

    @Getter
    @Setter
    //Внутренний класс, описывающий конфигурацию одного Kafka-консьюмера.
    public static class ConsumerConfiguration {
        private Map<String, String> properties = new HashMap<>();
        private String topic;
        private Duration pollTimeout = Duration.ofMillis(100);// Таймаут ожидания сообщений
        private int batchSize;// Размер батча для пакетной обработки сообщений
    }
}