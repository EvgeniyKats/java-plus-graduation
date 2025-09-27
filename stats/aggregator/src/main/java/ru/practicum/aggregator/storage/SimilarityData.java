package ru.practicum.aggregator.storage;

public record SimilarityData(
        long aId,
        long bId,
        double aSumWeight,
        double bSumWeight,
        double minWeight
) {
}
