package ru.practicum.event.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.event.event.model.Event;
import ru.practicum.event.event.service.param.GetEventAdminParam;
import ru.practicum.event.event.service.param.GetEventUserParam;
import ru.practicum.event.event.util.ResponseEventBuilder;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.feign.request.RequestInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;

import java.util.List;

import static ru.practicum.event.event.util.ValidatorEventTime.isEventTimeBad;
import static ru.practicum.interaction.Constants.EVENT_NOT_FOUND;
import static ru.practicum.interaction.dto.event.EventState.PUBLISHED;

/**
 * Сервис не работает напрямую с репозиторием и не содержит транзакции (@Transactional)
 * Для взаимодействия с доменной сущностью он использует EventTransactionService
 * Работа с внешними сервисами происходит через клиенты Feign
 * Благодаря разделению сокращается длительность и количество соединений с БД
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventTransactionService eventTransactionService;
    private final RequestInternalFeign requestInternalFeign;
    private final UserInternalFeign userInternalFeign;
    private final ResponseEventBuilder responseEventBuilder;

    @Override
    public List<EventFullDto> getEventsByAdmin(GetEventAdminParam param) {
        List<Event> events = eventTransactionService.getEventsByAdmin(param);
        return responseEventBuilder.buildManyEventResponseDto(events, EventFullDto.class);
    }

    @Override
    public List<EventShortDto> getEventsByUser(GetEventUserParam param) {
        List<Event> events = eventTransactionService.getEventsByUser(param);
        List<EventShortDto> eventDtos = responseEventBuilder.buildManyEventResponseDto(events, EventShortDto.class);

        if (param.getOnlyAvailable()) {
            eventDtos.removeIf(dto -> dto.getConfirmedRequests() == dto.getParticipantLimit());
        }

        return eventDtos;
    }

    @Override
    public List<EventShortDto> getAllUsersEvents(Long userId, Pageable page) {
        List<Event> events = eventTransactionService.getAllUsersEvents(userId, page);
        return responseEventBuilder.buildManyEventResponseDto(events, EventShortDto.class);
    }

    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto eventDto) {
        if (isEventTimeBad(eventDto.getEventDate(), 2)) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }
        userInternalFeign.findUserById(userId);
        Event event = eventTransactionService.createEvent(eventDto, userId);

        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public EventFullDto getEventForUser(Long userId, Long eventId) {
        Event event = eventTransactionService.getEventForUser(userId, eventId);
        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        Event event = eventTransactionService.updateEventByUser(userId, eventId, updateDto);
        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        userInternalFeign.findUserById(userId);
        return requestInternalFeign.findAllByEventId(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        userInternalFeign.findUserShortById(userId);

        Event event = eventTransactionService.getEventForUser(userId, eventId);

        if (isPreModerationOff(event.getRequestModeration(), event.getParticipantLimit())) {
            return new EventRequestStatusUpdateResult();
        }

        PatchManyRequestsStatusDto patchDto = new PatchManyRequestsStatusDto(updateRequest.getRequestIds(),
                updateRequest.getStatus(),
                eventId,
                event.getParticipantLimit());

        return requestInternalFeign.updateEventRequests(patchDto);
    }

    @Override
    public EventFullDto getEventById(Long eventId, boolean isInternalRequest) {
        Event event = eventTransactionService.getEventById(eventId);

        if (event.getState() != PUBLISHED) {
            if (isInternalRequest) {
                throw new ConflictException("Событие ещё не опубликовано.");
            } else {
                throw new NotFoundException(EVENT_NOT_FOUND);
            }
        }

        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateDto) {
        Event event = eventTransactionService.updateEventByAdmin(eventId, updateDto);
        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    private boolean isPreModerationOff(boolean moderationStatus, int limit) {
        return !moderationStatus || limit == 0;
    }
}
