package ru.practicum.event.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.interaction.dto.event.EventState;
import ru.practicum.event.event.model.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Boolean existsByCategoryId(Long catId);

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    Set<Event> findByIdIn(Set<Long> events);

    Optional<Event> findByIdAndState(Long eventId, EventState state);
}
