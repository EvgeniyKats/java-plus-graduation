package ru.practicum.event.event.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.RecommendationClientGrpc;
import ru.practicum.client.UserActionClientGrpc;
import ru.practicum.event.event.model.Event;
import ru.practicum.event.event.service.param.GetEventAdminParam;
import ru.practicum.event.event.service.param.GetEventUserParam;
import ru.practicum.event.event.util.ResponseEventBuilder;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;
import ru.practicum.interaction.dto.request.RequestStatus;
import ru.practicum.interaction.exception.AddLikeException;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.feign.request.RequestInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.event.event.util.ValidatorEventTime.isEventTimeBad;
import static ru.practicum.interaction.Constants.EVENT_NOT_FOUND;
import static ru.practicum.interaction.Constants.MAX_RECOMMENDATION_COUNT;
import static ru.practicum.interaction.dto.event.EventState.PUBLISHED;

/**
 * Сервис не работает напрямую с репозиторием и не содержит транзакции (@Transactional)
 * Для взаимодействия с доменной сущностью он использует EventTransactionService
 * Работа с внешними сервисами происходит через клиенты
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
    private final UserActionClientGrpc userActionClientGrpc;
    private final RecommendationClientGrpc recommendationClientGrpc;

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
    public List<EventFullDto> getRecommendations(Long userId) {
        Map<Long, Double> ratingEvent = new HashMap<>();

        List<Long> eventIds = recommendationClientGrpc.getRecommendationsForUser(userId, MAX_RECOMMENDATION_COUNT)
                .peek(proto -> ratingEvent.put(proto.getEventId(), proto.getScore()))
                .map(RecommendedEventProto::getEventId)
                .toList();

        List<Event> events = eventTransactionService.getEventsByIds(eventIds);

        return responseEventBuilder.buildResponseWithRating(events, ratingEvent, EventFullDto.class);
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
        validateUserExist(userId);

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
    public void addLike(Long userId, Long eventId) {
        validateUserExist(userId);
        Event event = eventTransactionService.getEventById(eventId);

        if (!event.getState().equals(PUBLISHED)) {
            throw new AddLikeException();
        }

        try {
            ParticipationRequestDto participation = requestInternalFeign.findUserParticipation(eventId, userId);

            if (!participation.getStatus().equals(RequestStatus.CONFIRMED)) {
                throw new AddLikeException();
            }

            Instant now = Instant.now();
            Timestamp ts = Timestamp.newBuilder()
                    .setSeconds(now.getEpochSecond())
                    .setNanos(now.getNano())
                    .build();
            userActionClientGrpc.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, ts);

        } catch (NotFoundException e) {
            throw new AddLikeException();
        }
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateDto) {
        Event event = eventTransactionService.updateEventByAdmin(eventId, updateDto);
        return responseEventBuilder.buildOneEventResponseDto(event, EventFullDto.class);
    }

    private void validateUserExist(long userId) {
        userInternalFeign.findUserShortById(userId);
    }

    private boolean isPreModerationOff(boolean moderationStatus, int limit) {
        return !moderationStatus || limit == 0;
    }
}
