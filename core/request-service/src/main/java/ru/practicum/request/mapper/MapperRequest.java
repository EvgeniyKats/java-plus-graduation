package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.request.model.ConfirmedRequests;
import ru.practicum.request.model.Request;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperRequest {
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    ConfirmedRequestsDto toConfirmedRequestsDto(ConfirmedRequests confirmedRequests);
}
