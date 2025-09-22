package ru.practicum.interaction.feign.comment;

import org.springframework.stereotype.Component;
import ru.practicum.interaction.feign.ServiceUnavailableException;

@Component
public class CommentFallbackException extends ServiceUnavailableException {
    public CommentFallbackException() {
        super("Сервис comment-service временно недоступен");
    }
}
