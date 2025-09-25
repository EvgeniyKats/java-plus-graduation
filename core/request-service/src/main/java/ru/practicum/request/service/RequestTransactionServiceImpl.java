package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventState;
import ru.practicum.interaction.dto.event.EventStatus;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;
import ru.practicum.interaction.dto.request.RequestStatus;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.DuplicateException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.request.mapper.MapperRequest;
import ru.practicum.request.model.ConfirmedRequests;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Все методы выполняются в транзакции целиком (@Transactional)
 * Использует только репозитории и мапперы
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RequestTransactionServiceImpl implements RequestTransactionService {
    private final RequestRepository requestRepository;
    private final MapperRequest mapperRequest;

    @Override
    public List<Request> getParticipationRequests(Long userId) {
        return requestRepository.findAllByRequesterId(userId);
    }

    @Override
    public Request findUserParticipationInEvent(Long userId, Long eventId) {
        return requestRepository.findByEventIdAndRequesterId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Запрос не найден"));
    }

    @Override
    public List<Request> findAllByEventId(Long eventId) {
        return requestRepository.findAllByEventId(eventId);
    }

    @Override
    public List<ConfirmedRequests> findConfirmedRequestByEventIds(Collection<Long> eventIds) {
        return requestRepository.getConfirmedRequests(eventIds, RequestStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public Request createParticipationRequest(EventFullDto event, Long userId) {
        if (requestRepository.existsByEventIdAndRequesterId(event.getId(), userId)) {
            throw new DuplicateException("Запрос на такое событие уже есть");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Невозможно создать запрос на неопубликованное событие");
        }

        if (event.getParticipantLimit() != 0
            && requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED)
               >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на событие");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Невозможно создать запрос будучи инициатором события");
        }

        boolean isPreModerationOn = isPreModerationOn(event.getRequestModeration(),
                event.getParticipantLimit());
        Request request = new Request(
                null,
                userId,
                event.getId(),
                isPreModerationOn ? RequestStatus.PENDING : RequestStatus.CONFIRMED,
                LocalDateTime.now()
        );

        requestRepository.save(request);

        return request;
    }

    @Override
    @Transactional
    public Request cancelParticipationRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден"));

        request.setStatus(RequestStatus.CANCELED);

        return request;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(PatchManyRequestsStatusDto patchDto) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        List<Request> requestsAll = requestRepository.findAllByEventId(patchDto.eventId());
        List<Request> requestsStatusPending = requestsAll.stream()
                .filter(req -> req.getStatus() == RequestStatus.PENDING)
                .filter(req -> patchDto.requestIds().contains(req.getId()))
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

        for (Request request : requestsStatusPending) {
            if (limitLeft > 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                ParticipationRequestDto dto = mapperRequest.toParticipationRequestDto(request);
                result.getConfirmedRequests().add(dto);
                limitLeft--;
            } else {
                request.setStatus(RequestStatus.CANCELED);
                ParticipationRequestDto dto = mapperRequest.toParticipationRequestDto(request);
                result.getRejectedRequests().add(dto);
            }
        }

        return result;
    }

    private boolean isPreModerationOn(boolean moderationStatus, int limit) {
        return moderationStatus && limit != 0;
    }
}
