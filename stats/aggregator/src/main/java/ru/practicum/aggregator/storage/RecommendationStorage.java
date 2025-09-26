package ru.practicum.aggregator.storage;

import java.util.List;

public interface RecommendationStorage {

    /**
     * Обновляет вес мероприятия и связанных с ним пар
     *
     * @param eventId идентификатор события
     * @param userId идентификатор пользователя
     * @param newWeight вес оценки пользователя
     */
    boolean updateWeight(long eventId, long userId, double newWeight);

    /**
     * @param eventId основное событие (событие А)
     * @return данные о сходствах для дальнейших расчётов
     */
    List<SimilarityData> getSimilarEvents(long eventId, long userId);
}
