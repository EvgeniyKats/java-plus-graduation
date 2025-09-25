package ru.practicum.request.service;

import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;
import ru.practicum.request.model.ConfirmedRequests;
import ru.practicum.request.model.Request;

import java.util.Collection;
import java.util.List;

public interface RequestTransactionService {
    List<Request> getParticipationRequests(Long userId);

    Request findUserParticipationInEvent(Long userId, Long eventId);

    List<Request> findAllByEventId(Long eventId);

    List<ConfirmedRequests> findConfirmedRequestByEventIds(Collection<Long> eventIds);

    Request createParticipationRequest(EventFullDto event, Long userId);

    Request cancelParticipationRequest(Long requestId);

    EventRequestStatusUpdateResult updateEventRequests(PatchManyRequestsStatusDto patchDto);

}
