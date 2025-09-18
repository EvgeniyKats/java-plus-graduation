package ru.practicum.main.service.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.api.event.EventInternalApi;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.logging.Logging;
import ru.practicum.main.service.event.service.EventService;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/internal/events")
public class InternalEventController implements EventInternalApi {
    private final EventService eventService;

    @Logging
    @Override
    public EventFullDto findById(Long id) {
        return eventService.getEventById(id, true);
    }
}
