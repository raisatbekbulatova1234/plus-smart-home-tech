package ru.yandex.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.AggregatorService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AggregationStarter implements ApplicationRunner {
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final AggregatorKafkaProperties properties;
    private final AggregatorService service;

    private final Duration pollTimeout;
    private final int batchSize;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public AggregationStarter(
            KafkaConsumer<String, SensorEventAvro> consumer,
            KafkaProducer<String, SensorsSnapshotAvro> producer,
            AggregatorKafkaProperties properties,
            AggregatorService service
    ) {
        this.consumer = consumer;
        this.producer = producer;
        this.properties = properties;
        this.service = service;

        this.pollTimeout = properties.getConsumer().getPollTimeout();
        this.batchSize = properties.getConsumer().getBatchSize();
    }

    @Override
    public void run(ApplicationArguments args) {
        start();
    }

    private void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(properties.getConsumer().getSensorTopic()));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(pollTimeout);
                int processedCount = 0;

                if (records.isEmpty()) {
                    continue;
                }

                try {
                    for (ConsumerRecord<String, SensorEventAvro> record : records) {
                        handleRecord(record, producer);
                        processedCount++;
                        manageOffsets(record, processedCount);
                    }

                    consumer.commitAsync();

                } catch (Exception ex) {
                    log.error("Ошибка обработки Kafka batch. recordsCount={}", records.count(), ex);
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception ex) {
            log.error("Критическая ошибка Kafka consumer loop: topic={}",
                    properties.getConsumer().getSensorTopic(), ex);
        } finally {

            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void handleRecord(ConsumerRecord<String, SensorEventAvro> record,
                              KafkaProducer<String, SensorsSnapshotAvro> producer) {
        String topic = properties.getProducer().getSnapshotTopic();

        service.updateState(record.value())
                .ifPresent(snapshot -> producer
                        .send(new ProducerRecord<>(topic, snapshot.getHubId(), snapshot)));
    }

    private void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int processedCount) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (processedCount % batchSize == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка commitAsync, делаем commitSync", exception);
                    consumer.commitSync(offsets);
                }
            });
        }
    }
}
