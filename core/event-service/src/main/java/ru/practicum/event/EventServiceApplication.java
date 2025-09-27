package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.event",
        "ru.practicum.logging",
        "ru.practicum.interaction.exception",
        "ru.practicum.interaction.feign",
        "ru.practicum.client"})
@EnableFeignClients(basePackages = {"ru.practicum.interaction.feign"})
public class EventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
