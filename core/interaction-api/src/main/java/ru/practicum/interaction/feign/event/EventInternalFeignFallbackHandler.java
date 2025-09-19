package ru.practicum.interaction.feign.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.event.EventFullDto;

@Component
@RequiredArgsConstructor
public class EventInternalFeignFallbackHandler implements EventInternalFeign {
    private final EventFallbackException eventFallbackException;

    @Override
    public EventFullDto findById(Long id) {
        throw eventFallbackException;
    }
}
