package ru.practicum.analyzer.similarity.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.kafka.KafkaConfig;
import ru.practicum.analyzer.similarity.service.SimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Arrays;

@Slf4j
@Component
public class SimilarityProcessor implements Runnable {
    private final SimilarityService similarityService;
    private final KafkaConfig.ConsumerSimilarityConfig consumerConfig;
    private final Consumer<String, EventSimilarityAvro> consumer;
    private volatile boolean running = true;

    public SimilarityProcessor(SimilarityService similarityService, KafkaConfig kafkaConfig) {
        this.similarityService = similarityService;
        this.consumerConfig = kafkaConfig.getConsumerSimilarityConfig();
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(consumerConfig.getTopics());

            // Цикл обработки событий
            while (running) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(consumerConfig.getPoolTimeout());
                // at-most-once позволяет избежать повторной обработки
                consumer.commitSync();
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    // обрабатываем очередную запись
                    handleRecord(record);
                }
            }
            log.info("Выполнение цикла было остановлено вручную");
        } catch (WakeupException e) {
            // лоигрование и закрытие консьюмера и продюсера в блоке finally
            log.warn("Возник WakeupException, running={}, msg={}, stackTrace={}",
                    running,
                    e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();
        }
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup();
        running = false;
    }

    private void handleRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        log.info("топик = {}, партиция = {}, смещение = {}, значение: {}",
                record.topic(), record.partition(), record.offset(), record.value());
        similarityService.putSimilarity(record.value());
    }
}
