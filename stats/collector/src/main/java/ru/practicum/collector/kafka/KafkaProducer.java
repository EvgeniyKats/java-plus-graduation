package ru.practicum.collector.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

public interface KafkaProducer<K, V> {
    void send(ProducerRecord<K, V> producerRecord);
}
