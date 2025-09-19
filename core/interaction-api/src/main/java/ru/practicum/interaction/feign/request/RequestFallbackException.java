package ru.practicum.interaction.feign.request;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.feign.ServiceUnavailableException;

@Component
public class RequestFallbackException extends ServiceUnavailableException {
    public RequestFallbackException() {
        super("Сервис request-service временно недоступен");
    }
}
