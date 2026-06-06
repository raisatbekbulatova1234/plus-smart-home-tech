package ru.yandex.practicum.kafka.deserialization;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

/**
 * Базовый десериализатор Avro сообщений для Kafka
 *
 * @param <T> тип Avro объекта, который будет десериализован (должен наследовать SpecificRecordBase)
 *
 * Этот класс реализует паттерн "Шаблонный метод" для десериализации всех Avro объектов.
 * Конкретные десериализаторы (HubEventDeserializer, SensorsSnapshotDeserializer)
 * будут наследовать этот класс и передавать свою схему.
 */
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final SpecificDatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            BinaryDecoder decoder =
                    decoderFactory.binaryDecoder(data, null);

            return reader.read(null, decoder);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Ошибка десериализации Avro сообщения из топика: " + topic, e
            );
        }
    }
}