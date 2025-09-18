package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.api.request.RequestInternalApi;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PathManyRequestsStatusDto;
import ru.practicum.logging.Logging;
import ru.practicum.request.service.RequestService;

import java.util.Collection;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class InternalRequestController implements RequestInternalApi {
    private final RequestService requestService;

    @Logging
    @Override
    public List<ParticipationRequestDto> findAllByEventId(Long eventId) {
        return requestService.findAllByEventId(eventId);
    }

    @Override
    public List<ConfirmedRequestsDto> findConfirmedRequestByEventIds(Collection<Long> eventIds) {
        return requestService.findConfirmedRequestByEventIds(eventIds);
    }

    @Logging
    @Override
    public EventRequestStatusUpdateResult updateEventRequests(PathManyRequestsStatusDto pathDto) {
        return requestService.updateEventRequests(pathDto);
    }
}
