package ru.practicum.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.comment",
        "ru.practicum.logging",
        "ru.practicum.interaction.exception",
        "ru.practicum.interaction.feign"})
@EnableFeignClients(basePackages = {"client", "ru.practicum.interaction.feign"})
public class CommentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
