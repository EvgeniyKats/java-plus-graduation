package ru.practicum.analyzer.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@ConfigurationProperties(prefix = "analyzer.kafka")
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConfig {
    ConsumerSimilarityConfig consumerSimilarityConfig;
    ConsumerInteractionConfig consumerInteractionConfig;

    public static class ConsumerSimilarityConfig extends BaseConsumerConfig {

        public ConsumerSimilarityConfig(Properties properties, List<String> topics, Duration poolTimeout) {
            super(properties, topics, poolTimeout);
        }
    }

    public static class ConsumerInteractionConfig extends BaseConsumerConfig {

        public ConsumerInteractionConfig(Properties properties, List<String> topics, Duration poolTimeout) {
            super(properties, topics, poolTimeout);
        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public abstract static class BaseConsumerConfig {
        Properties properties;
        List<String> topics;
        Duration poolTimeout;
    }
}
