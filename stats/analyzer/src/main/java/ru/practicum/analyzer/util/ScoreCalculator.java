package ru.practicum.analyzer.util;

import ru.practicum.analyzer.service.param.VisitedData;

import java.util.List;
import java.util.Map;

public interface ScoreCalculator {
    double calculateScore(long eventId,
                          Map<Long, Double> userWeights,
                          List<VisitedData> associations);
}
