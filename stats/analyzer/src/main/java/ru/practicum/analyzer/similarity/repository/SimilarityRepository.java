package ru.practicum.analyzer.similarity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.similarity.model.Similarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    Optional<Similarity> findByEventAAndEventB(long eventA, long eventB);

    @Query("SELECT s FROM Similarity s WHERE s.eventA IN :ids OR s.eventB IN :ids")
    List<Similarity> findAssociatedByEventIds(@Param("ids") Set<Long> ids);

    List<Similarity> findAllByEventB(long eventB);
}
