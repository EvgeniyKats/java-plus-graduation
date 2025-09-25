package ru.practicum.event.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.category.model.Category;
import ru.practicum.event.category.repository.CategoryRepository;
import ru.practicum.event.event.EventRepository;
import ru.practicum.event.event.LocationRepository;
import ru.practicum.event.event.MapperEvent;
import ru.practicum.event.event.model.Event;
import ru.practicum.event.event.model.QEvent;
import ru.practicum.event.event.service.param.GetEventAdminParam;
import ru.practicum.event.event.service.param.GetEventUserParam;
import ru.practicum.interaction.dto.event.EventState;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ru.practicum.event.event.util.ValidatorEventTime.isEventTimeBad;
import static ru.practicum.interaction.Constants.CATEGORY_NOT_FOUND;
import static ru.practicum.interaction.Constants.EVENT_NOT_FOUND;
import static ru.practicum.interaction.dto.event.EventState.CANCELED;
import static ru.practicum.interaction.dto.event.EventState.PENDING;
import static ru.practicum.interaction.dto.event.EventState.PUBLISHED;
import static ru.practicum.interaction.dto.event.EventState.REJECTED;
import static ru.practicum.interaction.dto.event.UpdateEventAdminRequest.StateAction.PUBLISH_EVENT;
import static ru.practicum.interaction.dto.event.UpdateEventUserRequest.StateAction.SEND_TO_REVIEW;

/**
 * Все методы выполняются в транзакции целиком (@Transactional)
 * Использует только репозитории и мапперы
 */
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class EventTransactionServiceImpl implements EventTransactionService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final MapperEvent eventMapper;

    @Override
    public List<Event> getEventsByAdmin(GetEventAdminParam param) {
        QEvent event = QEvent.event;
        BooleanBuilder requestBuilder = new BooleanBuilder();
        if (param.hasUsers()) {
            requestBuilder.and(event.initiatorId.in(param.getUsers()));
        }

        if (param.hasStates()) {
            requestBuilder.and(event.state.in(param.getStates()));
        }

        if (param.hasCategories()) {
            requestBuilder.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasRangeStart()) {
            requestBuilder.and(event.createdOn.gt(param.getRangeStart()));
        }

        if (param.hasRangeEnd()) {
            requestBuilder.and(event.createdOn.lt(param.getRangeEnd()));
        }

        return eventRepository.findAll(requestBuilder, param.getPage()).getContent();
    }

    @Override
    public List<Event> getEventsByUser(GetEventUserParam param) {
        QEvent event = QEvent.event;

        BooleanBuilder requestBuilder = new BooleanBuilder();

        requestBuilder.and(event.state.eq(PUBLISHED));

        if (param.hasText()) {
            BooleanExpression descriptionExpression = event.description.like(param.getText());
            BooleanExpression annotationExpression = event.annotation.like(param.getText());
            requestBuilder.andAnyOf(descriptionExpression, annotationExpression);
        }

        if (param.hasCategories()) {
            requestBuilder.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasPaid()) {
            requestBuilder.and(event.paid.eq(param.getPaid()));
        }

        requestBuilder.and(event.eventDate.gt(Objects.requireNonNullElseGet(param.getRangeStart(), LocalDateTime::now)));

        if (param.hasRangeEnd()) {
            requestBuilder.and(event.eventDate.lt(param.getRangeEnd()));
        }

        return eventRepository.findAll(requestBuilder, param.getPage()).getContent();
    }

    @Override
    public List<Event> getAllUsersEvents(Long userId, Pageable page) {
        return eventRepository.findByInitiatorId(userId, page);
    }

    @Override
    public List<Event> getEventsByIds(Collection<Long> eventIds) {
        List<Event> eventsFromDb = eventRepository.findAllById(eventIds);

        if (eventsFromDb.size() < eventIds.size()) {
            Set<Long> notFound = new HashSet<>(eventIds);

            for (Event event : eventsFromDb) {
                notFound.remove(event.getId());
            }

            throw new NotFoundException("Не найдены события: " + notFound);
        }
        return eventsFromDb;
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
    }

    @Override
    public Event getEventForUser(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
    }

    @Override
    @Transactional
    public Event createEvent(NewEventDto eventDto, Long userId) {
        Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(
                () -> new NotFoundException(CATEGORY_NOT_FOUND));

        Event eventFromDb = eventMapper.toEvent(eventDto, category, userId);
        eventFromDb.getLocation().setEvent(eventFromDb);
        locationRepository.save(eventFromDb.getLocation());

        eventRepository.save(eventFromDb);
        return eventFromDb;
    }

    @Override
    @Transactional
    public Event updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        EventState state = event.getState();
        if (state == PUBLISHED) {
            throw new ConflictException("Изменить можно только не опубликованные события, текущий статус " + state);
        }

        if (updateDto.hasStateAction()) {
            if (updateDto.getStateAction().equals(SEND_TO_REVIEW)) {
                event.setState(PENDING);
            } else {
                event.setState(CANCELED);
            }
        }

        if (updateDto.hasEventDate()) {
            if (isEventTimeBad(updateDto.getEventDate(), 2)) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за 2 часа от даты публикации");
            }
            event.setEventDate(updateDto.getEventDate());
        }

        if (updateDto.hasCategory()) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }

        eventMapper.updateEventByUserRequest(event, updateDto);
        return event;
    }

    @Override
    @Transactional
    public Event updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Изменить можно только события ожидающие модерацию, текущий статус " + event.getState());
        }

        if (updateDto.hasStateAction()) {
            if (updateDto.getStateAction() == PUBLISH_EVENT) {
                event.setState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                event.setState(REJECTED);
            }
        }

        if (updateDto.hasEventDate()) {
            if (isEventTimeBad(updateDto.getEventDate(), 1)) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(updateDto.getEventDate());
        }

        if (updateDto.hasCategory()) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }

        eventMapper.updateEventByAdminRequest(event, updateDto);

        return event;
    }
}
