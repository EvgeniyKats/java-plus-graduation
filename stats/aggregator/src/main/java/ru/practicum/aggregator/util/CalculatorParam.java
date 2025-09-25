package ru.practicum.aggregator.util;

import ru.practicum.aggregator.storage.SimilarityData;

import java.time.Instant;
import java.util.List;

public record CalculatorParam(
        List<SimilarityData> events,
        Instant timestamp
) {
}
