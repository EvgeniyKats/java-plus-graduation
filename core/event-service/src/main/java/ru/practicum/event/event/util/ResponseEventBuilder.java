package ru.practicum.event.event.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.event.event.MapperEvent;
import ru.practicum.event.event.model.Event;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.ResponseEvent;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.feign.comment.CommentInternalFeign;
import ru.practicum.interaction.feign.request.RequestInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ru.practicum.interaction.Constants.DEFAULT_COMMENTS_PAGEABLE;

// TODO: переработать
@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseEventBuilder {
    private final MapperEvent eventMapper;
    private final RequestInternalFeign requestInternalFeign;
    private final CommentInternalFeign commentInternalFeign;
    private final UserInternalFeign userInternalFeign;

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
        eventDto.setComments(getOneEventComments(eventId));
        return eventDto;
    }

    public <T extends ResponseEvent> List<T> buildManyEventResponseDto(List<Event> events, Class<T> type) {
        return null;
//        Map<Long, T> eventById = new HashMap<>();
//
//        Set<Long> userIds = events.stream()
//                .map(Event::getInitiatorId)
//                .collect(Collectors.toSet());
//
//        Map<Long, UserShortDto> userShortById = userInternalFeign.findManyUserShortsByIds(userIds);
//
//        for (Event event : events) {
//            if (type == EventFullDto.class) {
//                EventFullDto dtoTemp = eventMapper.toEventFullDto(event, userShortById.get(event.getInitiatorId()));
//                eventById.put(event.getId(), type.cast(dtoTemp));
//            } else {
//                EventShortDto dtoTemp = eventMapper.toEventShortDto(event, userShortById.get(event.getInitiatorId()));
//                eventById.put(event.getId(), type.cast(dtoTemp));
//            }
//        }
//
//        // заполнение подтвержденных запросов
//        List<ConfirmedRequestsDto> confirmedRequests = getManyEventsConfirmedRequests(eventById.keySet());
//
//        confirmedRequests.forEach(req -> {
//            long eventId = req.eventId();
//            int count = req.countRequests();
//            eventById.get(eventId).setConfirmedRequests(count);
//        });
//
//        // заполнение статистики просмотров
//        List<ViewStatsDto> viewStats = getManyEventsViews(eventById.keySet());
//
//        viewStats.forEach(stats -> {
//            long id = Long.parseLong(stats.getUri().replace("/events/", ""));
//            eventById.get(id).setViews(stats.getHits());
//        });
//
//        // заполнение комментариев
//        List<GetCommentDto> comments = getManyEventsComments(eventById.keySet());
//
//        comments.forEach(comment -> {
//            T event = eventById.get(comment.getEventId());
//            event.getComments().add(comment);
//        });
//
//        return new ArrayList<>(eventById.values());
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
        return -1;
//        StatParam statParam = StatParam.builder()
//                .start(created.minusMinutes(1))
//                .end(LocalDateTime.now().plusMinutes(1))
//                .unique(true)
//                .uris(List.of("/events/" + eventId))
//                .build();
//
//        List<ViewStatsDto> viewStats = statsClient.getStats(
//                statParam.getStart(),
//                statParam.getEnd(),
//                statParam.getUris(),
//                statParam.getUnique()
//        );
//
//        log.debug("Статистика пустая = {} . Одиночный от статистики по запросу uris = {}, start = {}, end = {}",
//                viewStats.isEmpty(),
//                statParam.getUris(),
//                statParam.getStart(),
//                statParam.getEnd());
//        return viewStats.isEmpty() ? 0 : viewStats.getFirst().getHits();
    }

    private List<GetCommentDto> getOneEventComments(long eventId) {
        return commentInternalFeign.findByEventId(eventId, DEFAULT_COMMENTS_PAGEABLE);
    }

    private List<Object> getManyEventsViews(Collection<Long> eventIds) {
        return null;
//        List<String> uris = eventIds.stream()
//                .map(id -> "/events/" + id)
//                .toList();
//
//        StatParam statParam = StatParam.builder()
//                .start(MIN_START_DATE)
//                .end(LocalDateTime.now().plusMinutes(1))
//                .unique(true)
//                .uris(uris)
//                .build();
//
//        List<ViewStatsDto> viewStats = statsClient.getStats(statParam.getStart(),
//                statParam.getEnd(),
//                statParam.getUris(),
//                statParam.getUnique()
//        );
//
//        log.debug("Получен ответ size = {}, массовый от статистики по запросу uris = {}, start = {}, end = {}",
//                viewStats.size(),
//                statParam.getUris(),
//                statParam.getStart(),
//                statParam.getEnd());
//        return viewStats;
    }

    private List<GetCommentDto> getManyEventsComments(Set<Long> eventsIds) {
        return commentInternalFeign.findLastCommentsForManyEvents(eventsIds);
    }
}
