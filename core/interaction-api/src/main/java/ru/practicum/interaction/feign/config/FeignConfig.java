package ru.practicum.interaction.feign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.interaction.Constants;
import ru.practicum.interaction.feign.FeignErrorDecoder;

import java.text.SimpleDateFormat;

@Configuration
public class FeignConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat(Constants.DATE_PATTERN));
        return objectMapper;
    }

    @Bean
    public Feign.Builder feignBuilderDecoder() {
        return Feign.builder()
                .errorDecoder(new FeignErrorDecoder());
    }
}
