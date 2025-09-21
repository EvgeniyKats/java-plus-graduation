package ru.practicum.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventState;
import ru.practicum.interaction.dto.event.EventStatus;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;
import ru.practicum.interaction.dto.request.RequestStatus;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.DuplicateException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.feign.event.EventInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;
import ru.practicum.request.mapper.MapperRequest;
import ru.practicum.request.model.ConfirmedRequests;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventInternalFeign eventInternalFeign;
    private final UserInternalFeign userInternalFeign;
    private final MapperRequest mapperRequest;

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream().map(mapperRequest::toParticipationRequestDto).toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {

        EventFullDto eventShortDto = eventInternalFeign.findById(eventId);

        UserDto user = userInternalFeign.findUserById(userId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicateException("Запрос на такое событие уже есть");
        }

        if (!eventShortDto.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Невозможно создать запрос на неопубликованное событие");
        }


        if (eventShortDto.getParticipantLimit() != 0
            && requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
               >= eventShortDto.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на событие");
        }

        if (eventShortDto.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Невозможно создать запрос будучи инициатором события");
        }

        boolean isPreModerationOn = isPreModerationOn(eventShortDto.getRequestModeration(),
                eventShortDto.getParticipantLimit());
        Request request = new Request(
                null,
                user.getId(),
                eventShortDto.getId(),
                isPreModerationOn ? RequestStatus.PENDING : RequestStatus.CONFIRMED,
                LocalDateTime.now()
        );

        request = requestRepository.save(request);

        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден"));

        userInternalFeign.findUserById(userId); // Выбросится исключение если пользователь не найден

        request.setStatus(RequestStatus.CANCELED);

        request = requestRepository.save(request);

        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventId(Long eventId) {
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(mapperRequest::toParticipationRequestDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(PatchManyRequestsStatusDto patchDto) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        List<Request> requestsAll = requestRepository.findAllByEventId(patchDto.eventId());
        List<Request> requestsStatusPending = requestsAll.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .filter(r -> patchDto.requestIds().contains(r.getId()))
                .toList();

        if (requestsStatusPending.size() != patchDto.requestIds().size()) {
            throw new ConflictException("Один или более запросов не находится в статусе PENDING");
        }

        if (patchDto.status().equals(EventStatus.REJECTED)) {
            for (Request request : requestsStatusPending) {
                request.setStatus(RequestStatus.REJECTED);
                ParticipationRequestDto dto = mapperRequest.toParticipationRequestDto(request);
                result.getRejectedRequests().add(dto);
            }

            return result;
        }

        long participantCount = requestsAll.stream()
                .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                .count();

        long limitLeft = patchDto.participantLimit() - participantCount;

        if (limitLeft == 0) {
            throw new ConflictException("Достигнут лимит заявок на событие");
        }

        int idx = 0;
        while (idx < requestsStatusPending.size() && limitLeft > 0) {
            Request request = requestsStatusPending.get(idx);
            request.setStatus(RequestStatus.CONFIRMED);

            ParticipationRequestDto dto = mapperRequest.toParticipationRequestDto(request);
            result.getConfirmedRequests().add(dto);

            limitLeft--;
            idx++;
        }

        while (idx < requestsStatusPending.size()) {
            Request request = requestsStatusPending.get(idx);
            request.setStatus(RequestStatus.CANCELED);

            ParticipationRequestDto dto = mapperRequest.toParticipationRequestDto(request);
            result.getRejectedRequests().add(dto);

            idx++;
        }

        return result;
    }

    @Override
    public List<ConfirmedRequestsDto> findConfirmedRequestByEventIds(Collection<Long> eventIds) {
        List<ConfirmedRequests> confirmedRequests =
                requestRepository.getConfirmedRequests(eventIds, RequestStatus.CONFIRMED);

        return confirmedRequests.stream()
                .map(mapperRequest::toConfirmedRequestsDto)
                .toList();
    }

    private boolean isPreModerationOn(boolean moderationStatus, int limit) {
        return moderationStatus && limit != 0;
    }
}
