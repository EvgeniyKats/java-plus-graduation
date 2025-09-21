package ru.practicum.interaction.dto.request;

import ru.practicum.interaction.dto.event.EventStatus;

import java.util.Set;

public record PatchManyRequestsStatusDto(
        Set<Long> requestIds,
        EventStatus status,
        Long eventId,
        Integer participantLimit
) {
}
