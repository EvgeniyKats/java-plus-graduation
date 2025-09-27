package ru.practicum.aggregator.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.producer.ProducerRecord;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProducerRecordBuilder<K, V> {
    String topic;
    Integer partition;
    Long timestamp;
    K key;
    V value;

    public ProducerRecord<K, V> build() {
        return new ProducerRecord<>(topic, partition, timestamp, key, value);
    }

    public ProducerRecordBuilder<K, V> setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public ProducerRecordBuilder<K, V> setPartition(Integer partition) {
        this.partition = partition;
        return this;
    }

    public ProducerRecordBuilder<K, V> setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ProducerRecordBuilder<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    public ProducerRecordBuilder<K, V> setValue(V value) {
        this.value = value;
        return this;
    }

    public static <K, V> ProducerRecordBuilder<K, V> newBuilder() {
        return new ProducerRecordBuilder<>();
    }
}
