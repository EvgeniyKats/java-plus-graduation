package ru.practicum.aggregator.util;

import org.springframework.stereotype.Component;
import ru.practicum.aggregator.storage.SimilarityData;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimilarityCalculatorAvro implements SimilarityCalculator<EventSimilarityAvro> {
    /**
     * Косинусное сходство между мероприятиями A и B:
     * similarity(A,B) = сумма минимальных весов A и B / (sqrt(сумма A) * sqrt(сумма B)
     */

    @Override
    public List<EventSimilarityAvro> calculate(CalculatorParam param) {
        if (param.events().isEmpty()) {
            return List.of(); // нет мероприятий для получения схожести
        }

        List<EventSimilarityAvro> ans = new ArrayList<>();

        for (SimilarityData data : param.events()) {
            double sqrtA = Math.sqrt(data.aSumWeight());
            double sqrtB = Math.sqrt(data.bSumWeight());
            double minOfAB = data.minWeight();

            double score = minOfAB / (sqrtA * sqrtB);

            EventSimilarityAvro similarity = EventSimilarityAvro.newBuilder()
                    .setEventA(Math.min(data.aId(), data.bId()))
                    .setEventB(Math.max(data.aId(), data.bId()))
                    .setScore(score)
                    .setTimestamp(param.timestamp())
                    .build();
            ans.add(similarity);
        }

        return ans;
    }
}
