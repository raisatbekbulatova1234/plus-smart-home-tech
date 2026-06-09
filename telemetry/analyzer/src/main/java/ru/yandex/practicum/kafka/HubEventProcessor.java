package ru.yandex.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.HandlerNotFoundException;
import ru.yandex.practicum.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final AnalyzerKafkaProperties properties;
    private final Map<Class<?>, HubEventHandler> hubEventHandlers;
    private final Duration pollTimeout;

    public HubEventProcessor(
            @Qualifier("hubEventConsumer")
            KafkaConsumer<String, HubEventAvro> consumer,
            AnalyzerKafkaProperties properties,
            List<HubEventHandler> handlers
    ) {
        this.consumer = consumer;
        this.properties = properties;
        this.hubEventHandlers = handlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getPayloadType,
                        Function.identity()
                ));

        this.pollTimeout = properties.getHubEventConfiguration().getPollTimeout();
    }

    @Override
    public void run() {
        start();
    }

    private void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(properties.getHubEventConfiguration().getTopic()));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(pollTimeout);

                if (records.isEmpty()) {
                    continue;
                }

                try {
                    for (ConsumerRecord<String, HubEventAvro> record : records) {
                        processRecord(record);
                    }

                    consumer.commitSync();

                } catch (Exception ex) {
                    log.error("Ошибка обработки Kafka batch. recordsCount={}", records.count(), ex);
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception ex) {
            log.error("Критическая ошибка Kafka consumer loop: topic={}",
                    properties.getHubEventConfiguration().getTopic(), ex);
        } finally {

            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private void processRecord(ConsumerRecord<String, HubEventAvro> record) {
            HubEventAvro event = record.value();

            Object payload = event.getPayload();
            HubEventHandler handler = hubEventHandlers.get(payload.getClass());

            if (handler == null) {
                log.warn("Нет handler для payload: {}", payload.getClass());
                throw new HandlerNotFoundException(String.format("Нет handler для payload: %s",
                        payload.getClass().getSimpleName()));
            }

            handler.handle(event);
    }
}
