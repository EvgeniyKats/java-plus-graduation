package ru.practicum.aggregator.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@ConfigurationProperties(prefix = "aggregator.kafka")
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConfig {
    ConsumerConfig consumerConfig;
    ProducerConfig producerConfig;

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class ConsumerConfig {
        Properties properties;
        List<String> topics;
        Duration poolTimeout;
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class ProducerConfig {
        Properties properties;
        String topic;
    }
}
