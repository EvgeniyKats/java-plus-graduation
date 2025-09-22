package ru.practicum.interaction.feign.user;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.feign.ServiceUnavailableException;

@Component
public class UserFallbackException extends ServiceUnavailableException {
    public UserFallbackException() {
        super("Сервис user-service временно недоступен");
    }
}
