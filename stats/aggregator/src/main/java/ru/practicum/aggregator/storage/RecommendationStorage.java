package ru.practicum.aggregator.storage;

import java.util.List;

public interface RecommendationStorage {
    /**
     * @return список идентификаторов связанных событий
     */
    List<Long> put(long eventId, long userId, double newWeight);

    /**
     * @param eventId       основное событие (событие А)
     * @param otherEventIds список событий, которые связаны с событием А. Не предполагается передача несвязанных событий.
     * @return данные о сходствах для дальнейших расчётов
     */
    List<SimilarityData> get(long eventId, List<Long> otherEventIds);
}
