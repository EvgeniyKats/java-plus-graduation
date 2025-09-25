package ru.practicum.aggregator.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.storage.RecommendationStorage;
import ru.practicum.aggregator.storage.SimilarityData;
import ru.practicum.aggregator.util.CalculatorParam;
import ru.practicum.aggregator.util.SimilarityCalculator;
import ru.practicum.aggregator.config.WeightCostByType;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RecommendationHandlerImpl implements RecommendationHandler {
    private final WeightCostByType weightCostByType;
    private final RecommendationStorage recommendationStorage;
    private final SimilarityCalculator<EventSimilarityAvro> similarityCalculator;

    @Override
    public List<EventSimilarityAvro> handleAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = weightCostByType.getActionWeight(action.getActionType());

        List<Long> updatedEventIds = recommendationStorage.put(eventId, userId, newWeight);
        List<SimilarityData> similarities = recommendationStorage.get(eventId, updatedEventIds);

        Instant ts = action.getTimestamp();
        CalculatorParam param = new CalculatorParam(similarities, ts);

        return similarityCalculator.calculate(param);
    }
}