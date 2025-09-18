package ru.practicum.interaction.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;

    private EventStatus status;
}
