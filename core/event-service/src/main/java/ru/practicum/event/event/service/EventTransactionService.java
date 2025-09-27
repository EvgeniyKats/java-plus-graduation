package ru.practicum.event.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.event.model.Event;
import ru.practicum.event.event.service.param.GetEventAdminParam;
import ru.practicum.event.event.service.param.GetEventUserParam;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;

import java.util.Collection;
import java.util.List;

public interface EventTransactionService {
    List<Event> getEventsByAdmin(GetEventAdminParam param);

    List<Event> getEventsByUser(GetEventUserParam param);

    List<Event> getAllUsersEvents(Long userId, Pageable page);

    List<Event> getEventsByIds(Collection<Long> eventIds);

    Event getEventById(Long eventId);

    Event getEventForUser(Long userId, Long eventId);

    Event createEvent(NewEventDto eventDto, Long userId);

    Event updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateDto);

    Event updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateDto);
}
