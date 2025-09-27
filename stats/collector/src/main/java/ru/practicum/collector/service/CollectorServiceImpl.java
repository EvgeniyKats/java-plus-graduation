package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import ru.practicum.collector.kafka.KafkaConfigProducer;
import ru.practicum.collector.kafka.KafkaProducer;
import ru.practicum.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.logging.Logging;

@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final KafkaProducer<Long, SpecificRecordBase> kafkaProducer;
    private final UserActionMapper userActionMapper;
    private final KafkaConfigProducer kafkaConfigProducer;

    @Override
    @Logging(Level.DEBUG)
    public void collectUserAction(UserActionProto actionProto) {
        UserActionAvro actionAvro = userActionMapper.toUserActionAvro(actionProto);

        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaConfigProducer.getTopics(),
                null,
                actionAvro.getTimestamp().toEpochMilli(),
                actionAvro.getUserId(),
                actionAvro);

        kafkaProducer.send(record);
    }
}
