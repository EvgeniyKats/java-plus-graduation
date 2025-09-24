package ru.practicum.aggregator.util.calculator;

import java.util.List;

public interface SimilarityCalculator<T> {
    List<T> calculate(CalculatorParam param);
}
