package ru.practicum.interaction.feign.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestInternalFeignFallbackHandler implements RequestInternalFeign {
    private final RequestFallbackException requestFallbackException;

    @Override
    public List<ParticipationRequestDto> findAllByEventId(Long eventId) {
        throw requestFallbackException;
    }

    @Override
    public ParticipationRequestDto findUserParticipation(Long eventId, Long userId) {
        throw requestFallbackException;
    }

    @Override
    public List<ConfirmedRequestsDto> findConfirmedRequestByEventIds(Collection<Long> eventIds) {
        throw requestFallbackException;
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(PatchManyRequestsStatusDto patchDto) {
        throw requestFallbackException;
    }
}
