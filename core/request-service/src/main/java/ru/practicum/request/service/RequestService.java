package ru.practicum.request.service;

import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;

import java.util.Collection;
import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getParticipationRequests(Long userId);

    ParticipationRequestDto findUserParticipationInEvent(Long userId, Long eventId);

    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> findAllByEventId(Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(PatchManyRequestsStatusDto patchDto);

    List<ConfirmedRequestsDto> findConfirmedRequestByEventIds(Collection<Long> eventIds);
}
