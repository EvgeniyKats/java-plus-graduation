package ru.practicum.main.service.event.util;

import client.StatParam;
import client.StatsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.ResponseEvent;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.feign.request.RequestInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;
import ru.practicum.main.service.comment.CommentRepository;
import ru.practicum.main.service.comment.MapperComment;
import ru.practicum.main.service.comment.model.Comment;
import ru.practicum.main.service.event.MapperEvent;
import ru.practicum.main.service.event.model.Event;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.interaction.Constants.DEFAULT_COMMENTS;
import static ru.practicum.interaction.Constants.MIN_START_DATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseEventBuilder {
    private final MapperEvent eventMapper;
    private final MapperComment commentMapper;
    private final RequestInternalFeign requestInternalFeign;
    private final CommentRepository commentRepository;
    private final UserInternalFeign userInternalFeign;
    private final StatsClient statsClient;

    public <T extends ResponseEvent> T buildOneEventResponseDto(Event event, Class<T> type) {
        T eventDto;

        UserShortDto userShortDto = userInternalFeign.findUserShortById(event.getInitiatorId());

        if (type == EventFullDto.class) {
            EventFullDto dtoTemp = eventMapper.toEventFullDto(event, userShortDto);
            eventDto = type.cast(dtoTemp);
        } else {
            EventShortDto dtoTemp = eventMapper.toEventShortDto(event, userShortDto);
            eventDto = type.cast(dtoTemp);
        }

        long eventId = event.getId();
        LocalDateTime created = event.getCreatedOn();

        eventDto.setConfirmedRequests(getOneEventConfirmedRequests(eventId));
        eventDto.setViews(getOneEventViews(created, eventId));
        eventDto.setComments(getOneEventComments(eventId, userShortDto));
        return eventDto;
    }

    public <T extends ResponseEvent> List<T> buildManyEventResponseDto(List<Event> events, Class<T> type) {
        Map<Long, T> eventById = new HashMap<>();

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortById = userInternalFeign.findManyUserShortsByIds(userIds);

        for (Event event : events) {
            if (type == EventFullDto.class) {
                EventFullDto dtoTemp = eventMapper.toEventFullDto(event, userShortById.get(event.getInitiatorId()));
                eventById.put(event.getId(), type.cast(dtoTemp));
            } else {
                EventShortDto dtoTemp = eventMapper.toEventShortDto(event, userShortById.get(event.getInitiatorId()));
                eventById.put(event.getId(), type.cast(dtoTemp));
            }
        }

        // заполнение подтвержденных запросов
        List<ConfirmedRequestsDto> confirmedRequests = getManyEventsConfirmedRequests(eventById.keySet());

        confirmedRequests.forEach(req -> {
            long eventId = req.eventId();
            int count = req.countRequests();
            eventById.get(eventId).setConfirmedRequests(count);
        });

        // заполнение статистики просмотров
        List<ViewStatsDto> viewStats = getManyEventsViews(eventById.keySet());

        viewStats.forEach(stats -> {
            long id = Long.parseLong(stats.getUri().replace("/events/", ""));
            eventById.get(id).setViews(stats.getHits());
        });

        // заполнение комментариев
        List<Comment> comments = getManyEventsComments(eventById.keySet());

        comments.forEach(comment -> {
            T t = eventById.get(comment.getEvent().getId());

            if (t.getComments() == null) {
                t.setComments(new ArrayList<>());
            }

            t.getComments().add(commentMapper.toGetCommentDto(comment, userShortById.get(comment.getAuthorId())));
        });

        return new ArrayList<>(eventById.values());
    }

    private int getOneEventConfirmedRequests(long eventId) {
        return requestInternalFeign.findConfirmedRequestByEventIds(List.of(eventId)).stream()
                .mapToInt(ConfirmedRequestsDto::countRequests)
                .sum();
    }

    private List<ConfirmedRequestsDto> getManyEventsConfirmedRequests(Collection<Long> eventIds) {
        return requestInternalFeign.findConfirmedRequestByEventIds(eventIds);
    }

    private long getOneEventViews(LocalDateTime created, long eventId) {
        StatParam statParam = StatParam.builder()
                .start(created.minusMinutes(1))
                .end(LocalDateTime.now().plusMinutes(1))
                .unique(true)
                .uris(List.of("/events/" + eventId))
                .build();

        List<ViewStatsDto> viewStats = statsClient.getStats(
                statParam.getStart(),
                statParam.getEnd(),
                statParam.getUris(),
                statParam.getUnique()
        );

        log.debug("Статистика пустая = {} . Одиночный от статистики по запросу uris = {}, start = {}, end = {}",
                viewStats.isEmpty(),
                statParam.getUris(),
                statParam.getStart(),
                statParam.getEnd());
        return viewStats.isEmpty() ? 0 : viewStats.getFirst().getHits();
    }

    private List<GetCommentDto> getOneEventComments(long eventId, UserShortDto userShortDto) {
        return commentRepository.findByEventId(eventId, DEFAULT_COMMENTS).stream()
                .map(comment -> commentMapper.toGetCommentDto(comment, userShortDto))
                .toList();
    }

    private List<ViewStatsDto> getManyEventsViews(Collection<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        StatParam statParam = StatParam.builder()
                .start(MIN_START_DATE)
                .end(LocalDateTime.now().plusMinutes(1))
                .unique(true)
                .uris(uris)
                .build();

        List<ViewStatsDto> viewStats = statsClient.getStats(statParam.getStart(),
                statParam.getEnd(),
                statParam.getUris(),
                statParam.getUnique()
        );

        log.debug("Получен ответ size = {}, массовый от статистики по запросу uris = {}, start = {}, end = {}",
                viewStats.size(),
                statParam.getUris(),
                statParam.getStart(),
                statParam.getEnd());
        return viewStats;
    }

    private List<Comment> getManyEventsComments(Set<Long> eventsIds) {
        return commentRepository.findLastCommentsForManyEvents(eventsIds);
    }
}
