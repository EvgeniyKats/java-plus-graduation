package ru.practicum.main.service.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.main.service.event.service.param.GetEventAdminParam;
import ru.practicum.main.service.event.service.param.GetEventUserParam;

import java.util.List;

public interface EventService {

    List<EventFullDto> getEventsByAdmin(GetEventAdminParam param);

    List<EventShortDto> getEventsByUser(GetEventUserParam param);

    EventFullDto getEventForUser(Long userId, Long eventId);

    List<EventShortDto> getAllUsersEvents(Long userId, Pageable page);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest eventDto);

    EventFullDto addNewEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventDto);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);

    /**
     * @param isInternalRequest влияет на тип исключения, если событие ещё не опубликовано,
     *                          true - ConflictException, false - NotFoundException
     * @throws ru.practicum.interaction.exception.NotFoundException событие не найдено или не опубликовано
     * @throws ru.practicum.interaction.exception.ConflictException событие не опубликовано
     */
    EventFullDto getEventById(Long eventId, boolean isInternalRequest);
}
