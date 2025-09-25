package ru.practicum.aggregator.util;

import java.util.List;

public interface SimilarityCalculator<T> {
    List<T> calculate(CalculatorParam param);
}
