package ru.practicum.analyzer.util;

import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.param.VisitedData;

import java.util.List;
import java.util.Map;

@Component
public class ScoreCalculatorImpl implements ScoreCalculator {
    @Override
    public double calculateScore(long associatedId, Map<Long, Double> userWeights, List<VisitedData> visited) {
        // предсказанная оценка = сумма всех (коэффициент подобия * оценка пользователя) / сумма всех коэффициентов подобия
        double weightSum = 0.0;
        double similaritySum = 0.0;

        for (VisitedData visitedData : visited) {
            double userWeight = userWeights.get(visitedData.visitedId());
            double similarityWeight = visitedData.similarity();
            weightSum += userWeight * similarityWeight;
            similaritySum += similarityWeight;
        }

        return similaritySum > 0 ? weightSum / similaritySum : 0.0;
    }
}
