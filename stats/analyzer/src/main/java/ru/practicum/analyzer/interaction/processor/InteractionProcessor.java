package ru.practicum.analyzer.interaction.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.kafka.KafkaConfig;
import ru.practicum.analyzer.interaction.service.InteractionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

@Slf4j
@Component
public class InteractionProcessor implements Runnable {
    private final InteractionService interactionService;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets;
    private final KafkaConfig.ConsumerInteractionConfig consumerConfig;
    private final Consumer<String, UserActionAvro> consumer;
    private volatile boolean running = true;

    public InteractionProcessor(InteractionService interactionService, KafkaConfig kafkaConfig) {
        this.interactionService = interactionService;
        this.consumerConfig = kafkaConfig.getConsumerInteractionConfig();
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.currentOffsets = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(consumerConfig.getTopics());

            // Цикл обработки событий
            while (running) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(consumerConfig.getPoolTimeout());
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
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // фиксируем синхронно последний обработанный оффсет для гарантий at-least-once
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
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
        interactionService.putAction(record.value());
    }
}
