package ru.practicum.event.event;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.event.category.model.Category;
import ru.practicum.event.event.model.Event;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.user.UserShortDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperEvent {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "category", target = "category")
    @Mapping(source = "initiatorId", target = "initiatorId")
    Event toEvent(NewEventDto newEventDto, Category category, Long initiatorId);

    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "userShortDto", target = "initiator")
    EventShortDto toEventShortDto(Event event, UserShortDto userShortDto);

    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "userShortDto", target = "initiator")
    EventFullDto toEventFullDto(Event event, UserShortDto userShortDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(source = "request.location.latitude", target = "location.latitude")
    @Mapping(source = "request.location.longitude", target = "location.longitude")
    void updateEventByAdminRequest(@MappingTarget Event event, UpdateEventAdminRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(source = "request.location.latitude", target = "location.latitude")
    @Mapping(source = "request.location.longitude", target = "location.longitude")
    void updateEventByUserRequest(@MappingTarget Event event, UpdateEventUserRequest request);
}
