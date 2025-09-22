package ru.practicum.interaction.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;

import java.util.Collection;
import java.util.List;

public interface RequestInternalApi {
    @GetMapping
    List<ParticipationRequestDto> findAllByEventId(@RequestParam @Positive Long eventId);

    @GetMapping("/confirmed")
    List<ConfirmedRequestsDto> findConfirmedRequestByEventIds(
            @RequestParam Collection<@Positive Long> eventIds);

    @PatchMapping
    EventRequestStatusUpdateResult updateEventRequests(@Valid @RequestBody PatchManyRequestsStatusDto patchDto);
}
