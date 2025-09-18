package ru.practicum.main.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.main.service",
        "ru.practicum.logging",
        "ru.practicum.interaction.exception",
        "ru.practicum.interaction.feign"})
@EnableFeignClients(basePackages = {"client", "ru.practicum.interaction.feign"})
public class MainServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainServiceApplication.class, args);
    }
}
