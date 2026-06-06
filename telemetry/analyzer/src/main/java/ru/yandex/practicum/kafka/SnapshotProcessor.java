package ru.yandex.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Процессор для чтения и обработки Kafka-сообщений со снапшотами состояния датчиков.
 *
 * Реализует Runnable, может быть запущен в отдельном потоке.
 *
 * Особенности:
 * - Пакетная обработка с коммитом смещений каждые batchSize сообщений
 * - Асинхронный коммит для повышения производительности
 * - Хранение текущих смещений в памяти для финального коммита
 */
@Slf4j
@Component
public class SnapshotProcessor implements Runnable {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final SnapshotService snapshotService;

    private final List<String> topics;        // Список топиков для подписки
    private final Duration pollTimeout;       // Таймаут polling'а
    private final int batchSize;              // Размер батча для коммита смещений

    // Хранилище текущих смещений по партициям
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public SnapshotProcessor(
            @Qualifier("snapshotConsumer")                     // Указываем конкретный бин консьюмера
            KafkaConsumer<String, SensorsSnapshotAvro> consumer,
            AnalyzerKafkaProperties properties,
            SnapshotService snapshotService
    ) {
        this.consumer = consumer;
        this.snapshotService = snapshotService;

        // Извлекаем настройки из конфигурации
        this.topics = List.of(properties.getSnapshotConfiguration().getTopic());
        this.pollTimeout = properties.getSnapshotConfiguration().getPollTimeout();
        this.batchSize = properties.getSnapshotConfiguration().getBatchSize();
    }

    @Override
    public void run() {
        start();    // Запуск основного цикла обработки
    }

    /**
     * Запускает бесконечный цикл чтения сообщений из Kafka.
     *
     * Логика:
     * 1. Регистрирует shutdown hook для корректного завершения
     * 2. Подписывается на топики
     * 3. В цикле читает сообщения
     * 4. Обрабатывает каждое сообщение через SnapshotService
     * 5. Управляет смещениями (коммит каждые batchSize сообщений)
     * 6. При завершении - финальный коммит и закрытие консьюмера
     */
    private void start() {
        // Регистрируем хук
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);

                if (records.isEmpty()) {
                    continue;
                }

                try {
                    // Обрабатываем все сообщения в пакете
                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        snapshotService.handleSnapshot(record.value());
                    }

                    // Один раз обновляем оффсеты после успешной обработки ВСЕЙ пачки
                    updateOffsets(records);

                    // Асинхронный коммит с обработкой ошибок
                    consumer.commitAsync((offsets, exception) -> {
                        if (exception != null) {
                            log.error("Ошибка асинхронного коммита оффсетов", exception);
                        } else {
                            log.debug("Успешно закоммичены оффсеты: {}", offsets);
                        }
                    });

                } catch (Exception ex) {
                    log.error("Ошибка обработки Kafka batch. recordsCount={}", records.count(), ex);
                    // При ошибке смещения НЕ коммитятся - сообщения будут обработаны заново
                }
            }

        } catch (WakeupException ignored) {
            log.info("Consumer остановлен по сигналу");
        } catch (Exception ex) {
            log.error("Критическая ошибка Kafka consumer loop: topic={}", topics, ex);
        } finally {

            log.info("Закрываем консьюмер");

            try {
                // Финальный синхронный коммит всех накопленных смещений
                if (!currentOffsets.isEmpty()) {
                    consumer.commitSync(currentOffsets);
                }
            } catch (Exception e) {
                log.error("Ошибка при финальном коммите", e);
            }

            try {
                consumer.close();
            } catch (Exception e) {
                log.error("Ошибка при закрытии consumer", e);
            }
        }
    }

    private void updateOffsets(ConsumerRecords<String, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
            currentOffsets.put(
                    new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1, "snapshot-processed")
            );
        }
    }

    /**
     * Управляет смещениями для точного контроля коммита.
     *
     * Логика:
     * 1. Сохраняет текущее смещение для партиции (offset + 1 = следующее сообщение для чтения)
     * 2. Каждые batchSize сообщений делает асинхронный коммит накопленных смещений
     *
     * @param record - обработанная запись
     * @param processedCount - количество обработанных сообщений в текущем batch'е
     */
    private void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int processedCount) {
        // Сохраняем смещение для партиции (offset + 1 = следующее сообщение)
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),  // Ключ: топик + партиция
                new OffsetAndMetadata(record.offset() + 1)              // Значение: следующее смещение
        );

        // Каждые batchSize сообщений делаем асинхронный коммит
        if (processedCount % batchSize == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}