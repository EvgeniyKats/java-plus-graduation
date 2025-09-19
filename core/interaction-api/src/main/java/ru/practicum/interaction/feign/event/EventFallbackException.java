package ru.practicum.interaction.feign.event;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.feign.ServiceUnavailableException;

@Component
public class EventFallbackException extends ServiceUnavailableException {
    public EventFallbackException() {
        super("Сервис event-service временно недоступен");
    }
}
