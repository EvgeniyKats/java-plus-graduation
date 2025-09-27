package ru.practicum.aggregator;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.kafka.KafkaConfig;
import ru.practicum.aggregator.kafka.ProducerRecordBuilder;
import ru.practicum.aggregator.handler.RecommendationHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
public class AggregationStarter implements CommandLineRunner {
    private final RecommendationHandler recommendationHandler;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets;

    private final Consumer<String, UserActionAvro> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaConfig kafkaConfig;

    private volatile boolean running = true;

    public AggregationStarter(RecommendationHandler recommendationHandler, KafkaConfig kafkaConfig) {
        this.recommendationHandler = recommendationHandler;
        this.currentOffsets = new HashMap<>();
        this.kafkaConfig = kafkaConfig;
        this.producer = new KafkaProducer<>(kafkaConfig.getProducerConfig().getProperties());
        this.consumer = new KafkaConsumer<>(kafkaConfig.getConsumerConfig().getProperties());
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий,
     * формирует снимок их состояния и записывает в кафку.
     */
    @Override
    public void run(String... args) {
        try {
            consumer.subscribe(kafkaConfig.getConsumerConfig().getTopics());

            // Цикл обработки событий
            while (running) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(kafkaConfig.getConsumerConfig().getPoolTimeout());
                int count = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    // обрабатываем очередную запись
                    handleRecord(record);
                    // фиксируем оффсеты обработанных записей, если нужно
                    manageOffsets(record, count);
                    count++;
                }
                // at-least-once для наибольшего сообщения, асинхронно, синхронная фиксация в блоке finally
                consumer.commitAsync();
            }
            log.info("Выполнение цикла было остановлено вручную");
        } catch (WakeupException e) {
            // лоигрование и закрытие консьюмера и продюсера в блоке finally
            log.warn("Возник WakeupException, running={}, msg={}, stackTrace={}",
                    running,
                    e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            log.error("Ошибка во время обработки", e);
        } finally {
            try {
                // очистка буфера
                producer.flush();
                // фиксируем синхронно последний обработанный оффсет для гарантий at-least-once
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup();
        running = false;
    }

    private void manageOffsets(ConsumerRecord<String, UserActionAvro> record, int count) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1));

        if (count % 10 == 0) {
            log.debug("count={}", count);
            OptionalLong maxOptional = currentOffsets.values().stream()
                    .mapToLong(OffsetAndMetadata::offset)
                    .max();
            maxOptional.ifPresent(max -> log.debug("Фиксация оффсетов max={}", max));

            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception == null) {
                    log.debug("Успешная фиксация оффсетов: {}", offsets);
                } else {
                    log.error("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, UserActionAvro> record) {
        log.info("топик = {}, партиция = {}, смещение = {}, значение: {}",
                record.topic(), record.partition(), record.offset(), record.value());

        List<EventSimilarityAvro> similarities = recommendationHandler.handleAction(record.value());

        if (!similarities.isEmpty()) {
            log.info("Схожести были обновлены, отправка");
            similarities.forEach(similarity -> {
                ProducerRecordBuilder<String, SpecificRecordBase> recordSendBuilder
                        = ProducerRecordBuilder.<String, SpecificRecordBase>newBuilder()
                        .setTopic(kafkaConfig.getProducerConfig().getTopic())
                        .setTimestamp(similarity.getTimestamp().toEpochMilli())
                        .setKey(String.valueOf(similarity.getEventA()))
                        .setValue(similarity);
                producer.send(recordSendBuilder.build());
            });
            log.info("Отправка схожестей завершена");
        } else {
            log.info("Схожести не обновлены, действие не оказало влияния");
        }
    }
}