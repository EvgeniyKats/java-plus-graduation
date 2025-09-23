package ru.practicum.collector.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerImpl implements KafkaProducer<Long, SpecificRecordBase> {
    private final Producer<Long, SpecificRecordBase> producer;

    private KafkaProducerImpl(KafkaConfigProducer kafkaConfigProducer) {
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(kafkaConfigProducer.getProperties());
    }

    @Override
    public void send(ProducerRecord<Long, SpecificRecordBase> record) {
        producer.send(record);
    }

    @PreDestroy
    private void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}
