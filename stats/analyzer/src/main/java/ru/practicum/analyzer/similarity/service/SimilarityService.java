package ru.practicum.analyzer.similarity.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface SimilarityService {
    void putSimilarity(EventSimilarityAvro similarity);
}
