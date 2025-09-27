package ru.practicum.aggregator.handler;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface RecommendationHandler {
    List<EventSimilarityAvro> handleAction(UserActionAvro action);
}
