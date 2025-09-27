package ru.practicum.analyzer.interaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.interaction.model.Interaction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByUserIdAndEventId(long userId, long eventId);

    List<Interaction> findAllByUserIdAndEventIdIn(long userId, Set<Long> eventIds);

    List<Interaction> findAllByUserId(long userId);

    List<Interaction> findAllByEventIdIn(Set<Long> eventIds);
}
