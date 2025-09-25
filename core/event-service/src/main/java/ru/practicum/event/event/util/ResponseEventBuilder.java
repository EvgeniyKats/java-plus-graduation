package ru.practicum.event.event.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.RecommendationClientGrpc;
import ru.practicum.event.event.MapperEvent;
import ru.practicum.event.event.model.Event;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.ResponseEvent;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.feign.comment.CommentInternalFeign;
import ru.practicum.interaction.feign.request.RequestInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.interaction.Constants.DEFAULT_COMMENTS_PAGEABLE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseEventBuilder {
    private final MapperEvent eventMapper;
    private final RequestInternalFeign requestInternalFeign;
    private final CommentInternalFeign commentInternalFeign;
    private final UserInternalFeign userInternalFeign;
    private final RecommendationClientGrpc recommendationClientGrpc;

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
        eventDto.setConfirmedRequests(getOneEventConfirmedRequests(eventId));
        eventDto.setRating(getOneEventRating(eventId));
        eventDto.setComments(getOneEventComments(eventId));
        return eventDto;
    }

    public <T extends ResponseEvent> List<T> buildManyEventResponseDto(List<Event> events, Class<T> type) {
        Collection<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Double> eventRatings = getManyEventsRecommendations(eventIds)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));

        return buildResponseWithRating(events, eventRatings, type);
    }

    public <T extends ResponseEvent> List<T> buildResponseWithRating(List<Event> events,
                                                                     Map<Long, Double> ratingEvent,
                                                                     Class<T> type) {
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

        // заполнение рейтинга
        ratingEvent.forEach((id, rating) -> eventById.get(id).setRating(rating));

        // заполнение комментариев
        List<GetCommentDto> comments = getManyEventsComments(eventById.keySet());

        comments.forEach(comment -> {
            T event = eventById.get(comment.getEventId());
            event.getComments().add(comment);
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

    private double getOneEventRating(long eventId) {
        return recommendationClientGrpc.getInteractionsCount(List.of(eventId))
                .mapToDouble(RecommendedEventProto::getScore)
                .findFirst()
                .orElse(0.0);
    }

    private List<GetCommentDto> getOneEventComments(long eventId) {
        return commentInternalFeign.findByEventId(eventId, DEFAULT_COMMENTS_PAGEABLE);
    }

    private Stream<RecommendedEventProto> getManyEventsRecommendations(Collection<Long> eventIds) {
        return recommendationClientGrpc.getInteractionsCount(eventIds);
    }

    private List<GetCommentDto> getManyEventsComments(Set<Long> eventsIds) {
        return commentInternalFeign.findLastCommentsForManyEvents(eventsIds);
    }
}
