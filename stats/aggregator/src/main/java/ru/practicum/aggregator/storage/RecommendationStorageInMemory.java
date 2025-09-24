package ru.practicum.aggregator.storage;

import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RecommendationStorageInMemory implements RecommendationStorage {
    // отображение пары событий на их минимальный вес (сумма минимальных весов всех пользователей) - числитель
    private final Map<TwoEventWeightMin, Double> minWeights;

    // отображение события на сумму весов (как событие оценили все пользователи) - знаменатель
    private final Map<Long, Double> sumWeights;

    // отображение события и пользователя на вес (как пользователь оценил событие)
    private final Map<EventUserWeight, Double> userWeights;

    public RecommendationStorageInMemory() {
        this.minWeights = new HashMap<>();
        this.sumWeights = new HashMap<>();
        this.userWeights = new HashMap<>();
    }

    public List<Long> put(long eventId, long userId, double newWeight) {
        EventUserWeight key = new EventUserWeight(eventId, userId);
        double oldWeight = userWeights.getOrDefault(key, 0.0);

        if (newWeight <= oldWeight) {
            return List.of(); // обновление не требуется
        }

        // 1. обновляем вес пользователя
        userWeights.put(key, newWeight);
        // 2. обновляем общий вес мероприятия (знаменатель)
        sumWeights.put(eventId, sumWeights.getOrDefault(eventId, 0.0) - oldWeight + newWeight);
        // 3. обновляем вес пар мероприятий (числитель)
        List<Long> ans = new ArrayList<>();

        for (Long otherEventId : sumWeights.keySet()) {
            if (otherEventId == eventId) {
                continue; // не сравниваем пары А_А (текущее с текущим)
            }

            // если пользователь как-то оценивал мероприятие Б, его необходимо добавить в ответ
            EventUserWeight otherKey = new EventUserWeight(otherEventId, userId);
            if (userWeights.containsKey(otherKey)) {
                ans.add(otherEventId);

                double otherWeight = userWeights.get(otherKey);
                double minOld = Math.min(otherWeight, oldWeight);
                double newMin = Math.min(otherWeight, newWeight);

                if (newMin > minOld) {
                    TwoEventWeightMin twoKey = new TwoEventWeightMin(eventId, otherEventId);
                    minWeights.put(twoKey, minWeights.getOrDefault(twoKey, 0.0) - minOld + newMin);
                }
            }
        }

        return ans;
    }

    public List<SimilarityData> get(long eventId, List<Long> otherEventIds) {
        if (otherEventIds.isEmpty()) {
            return List.of();
        }

        List<SimilarityData> events = new ArrayList<>();
        double thisSum = sumWeights.get(eventId);

        for (Long otherEventId : otherEventIds) {
            double otherSum = sumWeights.get(otherEventId);
            double minWeight = minWeights.get(new TwoEventWeightMin(eventId, otherEventId));
            events.add(new SimilarityData(eventId, otherEventId, thisSum, otherSum, minWeight));
        }

        return events;
    }

    /**
     * Образует пару событие-пользователь
     */
    @EqualsAndHashCode
    private static class EventUserWeight {
        final Long eventId;
        final Long userId;

        public EventUserWeight(Long eventId, Long userId) {
            this.eventId = eventId;
            this.userId = userId;
        }
    }

    /**
     * Упорядочивает пары событий для избежания дублирования.
     */
    @EqualsAndHashCode
    private static class TwoEventWeightMin {
        final Long minId;
        final Long maxId;

        public TwoEventWeightMin(Long a, Long b) {
            this.minId = Math.min(a, b);
            this.maxId = Math.max(a, b);
        }
    }
}
