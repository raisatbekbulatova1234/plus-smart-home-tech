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

                if (records.isEmpty()) {
                    continue;
                }

                // Обработка пачки с возможностью батчирования коммитов
                processBatch(records);
            }
        } catch (WakeupException e) {
            log.info("Корректное завершение consumer");
        } catch (Exception e) {
            log.error("Критическая ошибка", e);
        } finally {
            closeResources();
        }
    }

    private void processBatch(ConsumerRecords<String, SensorEventAvro> records) {
        int processedCount = 0;

        try {
            // Обрабатываем все записи и обновляем оффсеты
            for (ConsumerRecord<String, SensorEventAvro> record : records) {
                handleRecord(record, producer);
                processedCount++;

                // Обновляем оффсет для каждой записи
                updateOffset(record);

                // Коммитим каждые batchSize записей
                if (processedCount % batchSize == 0) {
                    commitOffsets();
                }
            }

            // Коммитим оставшиеся оффсеты после обработки всей пачки
            if (processedCount % batchSize != 0) {
                commitOffsets();
            }

        } catch (Exception e) {
            log.error("Ошибка обработки батча размером {}", records.count(), e);
            // При ошибке не коммитим - записи обработаются при следующем poll()
            // currentOffsets остаются на последнем успешно закоммиченном оффсете
        }
    }

    /**
     * Обновляет оффсет для конкретной записи
     * Следующая запись будет с offset + 1
     */
    private void updateOffset(ConsumerRecord<String, SensorEventAvro> record) {
        TopicPartition partition = new TopicPartition(record.topic(), record.partition());
        OffsetAndMetadata newOffset = new OffsetAndMetadata(record.offset() + 1);
        currentOffsets.put(partition, newOffset);
    }

    /**
     * Коммитит текущие оффсеты асинхронно с fallback на синхронный
     */
    private void commitOffsets() {
        if (currentOffsets.isEmpty()) {
            return;
        }

        // Делаем копию оффсетов для коммита
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>(currentOffsets);

        consumer.commitAsync(offsetsToCommit, (offsets, exception) -> {
            if (exception != null) {
                log.warn("Ошибка асинхронного коммита, пробуем синхронный", exception);
                try {
                    consumer.commitSync(offsets);
                    log.info("Синхронный коммит успешен");
                } catch (Exception e) {
                    log.error("Синхронный коммит также не удался", e);
                }
            } else {
                log.debug("Успешно закоммичены оффсеты: {}", offsets);
            }
        });
    }

    private void handleRecord(ConsumerRecord<String, SensorEventAvro> record,
                              KafkaProducer<String, SensorsSnapshotAvro> producer) {
        String topic = properties.getProducer().getSnapshotTopic();

        service.updateState(record.value())
                .ifPresent(snapshot -> {
                    ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                            new ProducerRecord<>(topic, snapshot.getHubId(), snapshot);
                    producer.send(producerRecord, (metadata, exception) -> {
                        if (exception != null) {
                            log.error("Ошибка отправки снапшота для hubId: {}",
                                    snapshot.getHubId(), exception);
                        } else {
                            log.debug("Снапшот отправлен: topic={}, partition={}, offset={}",
                                    metadata.topic(), metadata.partition(), metadata.offset());
                        }
                    });
                });
    }

    private void closeResources() {
        log.info("Закрытие ресурсов Kafka");

        // Закрываем producer
        if (producer != null) {
            try {
                producer.flush();
                producer.close();
                log.info("Producer закрыт");
            } catch (Exception e) {
                log.error("Ошибка закрытия producer", e);
            }
        }

        // Закрываем consumer
        if (consumer != null) {
            try {
                // Финальный синхронный коммит
                if (!currentOffsets.isEmpty()) {
                    log.info("Финальный коммит оффсетов: {}", currentOffsets);
                    consumer.commitSync(currentOffsets);
                }
                consumer.close();
                log.info("Consumer закрыт");
            } catch (Exception e) {
                log.error("Ошибка закрытия consumer", e);
            }
        }
    }
}