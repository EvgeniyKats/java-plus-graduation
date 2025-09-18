package ru.practicum.interaction.api.event;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.event.EventFullDto;

public interface EventInternalApi {
    @GetMapping("/{id}")
    EventFullDto findById(@RequestParam @Positive Long id);
}
