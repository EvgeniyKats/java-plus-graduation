package ru.practicum.collector.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ConfigurationProperties(prefix = "collector.kafka.producer-config")
public class KafkaConfigProducer {
    Properties properties;
    String topics;
}
