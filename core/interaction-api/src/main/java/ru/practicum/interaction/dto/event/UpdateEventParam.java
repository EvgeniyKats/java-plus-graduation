package ru.practicum.interaction.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateEventParam {
    String title;

    String description;

    String annotation;

    Long category;

    LocalDateTime createdOn;

    Boolean paid;

    Integer participantLimit;

    Boolean requestModeration;

    LocationDto location;
}
