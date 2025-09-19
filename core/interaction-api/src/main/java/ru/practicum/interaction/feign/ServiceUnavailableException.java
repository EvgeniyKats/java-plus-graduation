package ru.practicum.interaction.feign;

import lombok.Getter;

@Getter
public class ServiceUnavailableException extends RuntimeException {
    private final String message;

    public ServiceUnavailableException(String message) {
        super(message);
        this.message = message;
    }
}
