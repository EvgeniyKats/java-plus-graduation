package ru.practicum.event.event.controller;

import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.UserActionClientGrpc;
import ru.practicum.event.event.service.EventService;
import ru.practicum.event.event.service.param.GetEventUserParam;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.EventSortType;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.logging.Logging;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.interaction.Constants.DATE_PATTERN;
import static ru.practicum.interaction.Constants.EWM_USER_ID_HEADER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final UserActionClientGrpc userActionClientGrpc;

    @GetMapping
    @Logging
    public List<EventShortDto> getEventsByFilters(@RequestParam(name = "text", required = false) String text,
                                                  @RequestParam(name = "categories", required = false) List<Long> categories,
                                                  @RequestParam(name = "paid", required = false) Boolean paid,
                                                  @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
                                                  @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd,
                                                  @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(name = "sort", required = false) EventSortType sort,
                                                  @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                                  @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeStart > rangeEnd");
        }

        Pageable page;
        if (sort != null) {
            Sort sortType = switch (sort) {
                case EVENT_DATE -> Sort.by("createdOn").ascending();
                case VIEWS -> Sort.by("views").ascending();
            };
            page = PageRequest.of(from, size, sortType);
        } else {
            page = PageRequest.of(from, size);
        }

        GetEventUserParam param = GetEventUserParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .page(page)
                .build();

        return eventService.getEventsByUser(param);
    }

    @GetMapping("/{eventId}")
    @Logging
    public EventFullDto getEventById(@PathVariable @Positive Long eventId,
                                     @RequestHeader(EWM_USER_ID_HEADER) @Positive Long userId) {

        EventFullDto event = eventService.getEventById(eventId, false);

        Instant now = Instant.now();
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        userActionClientGrpc.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW, ts);

        return event;
    }

    @GetMapping("/recommendations")
    @Logging
    public List<EventFullDto> getRecommendations(@RequestHeader(EWM_USER_ID_HEADER) @Positive Long userId) {
        return eventService.getRecommendations(userId);
    }

    @PutMapping("/{eventId}/like")
    @Logging
    public void addLike(@RequestHeader(EWM_USER_ID_HEADER) @Positive Long userId,
                        @PathVariable @Positive Long eventId) {
        eventService.addLike(userId, eventId);
    }
}
