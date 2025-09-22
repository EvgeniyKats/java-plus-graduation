package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ConfirmedRequestsDto;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.PatchManyRequestsStatusDto;
import ru.practicum.interaction.feign.event.EventInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;
import ru.practicum.request.mapper.MapperRequest;
import ru.practicum.request.model.ConfirmedRequests;
import ru.practicum.request.model.Request;

import java.util.Collection;
import java.util.List;

/**
 * Сервис не работает напрямую с репозиторием и не содержит транзакции (@Transactional)
 * Для взаимодействия с доменной сущностью он использует RequestTransactionService
 * Работа с внешними сервисами происходит через клиенты Feign
 * Благодаря разделению сокращается длительность и количество соединений с БД
 */
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestTransactionService requestTransactionService;
    private final EventInternalFeign eventInternalFeign;
    private final UserInternalFeign userInternalFeign;
    private final MapperRequest mapperRequest;

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        List<Request> requests = requestTransactionService.getParticipationRequests(userId);
        return requests.stream().map(mapperRequest::toParticipationRequestDto).toList();
    }

    @Override
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        EventFullDto event = eventInternalFeign.findById(eventId);
        validateUserExist(userId);
        Request request = requestTransactionService.createParticipationRequest(event, userId);
        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        validateUserExist(userId);
        Request request = requestTransactionService.cancelParticipationRequest(requestId);
        return mapperRequest.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventId(Long eventId) {
        List<Request> requests = requestTransactionService.findAllByEventId(eventId);
        return requests.stream().map(mapperRequest::toParticipationRequestDto).toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(PatchManyRequestsStatusDto patchDto) {
        return requestTransactionService.updateEventRequests(patchDto);
    }

    @Override
    public List<ConfirmedRequestsDto> findConfirmedRequestByEventIds(Collection<Long> eventIds) {
        List<ConfirmedRequests> confirmedRequests = requestTransactionService.findConfirmedRequestByEventIds(eventIds);

        return confirmedRequests.stream()
                .map(mapperRequest::toConfirmedRequestsDto)
                .toList();
    }

    private void validateUserExist(Long userId) {
        userInternalFeign.findUserById(userId); // выбросится исключение если пользователь не найден
    }
}
