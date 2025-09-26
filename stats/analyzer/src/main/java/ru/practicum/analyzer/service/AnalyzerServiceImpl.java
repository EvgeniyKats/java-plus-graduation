package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.config.Constant;
import ru.practicum.analyzer.interaction.model.Interaction;
import ru.practicum.analyzer.interaction.repository.InteractionRepository;
import ru.practicum.analyzer.service.param.VisitedData;
import ru.practicum.analyzer.similarity.model.Similarity;
import ru.practicum.analyzer.similarity.repository.SimilarityRepository;
import ru.practicum.analyzer.util.ScoreCalculator;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyzerServiceImpl implements AnalyzerService {
    private final SimilarityRepository similarityRepository;
    private final InteractionRepository interactionRepository;
    private final ScoreCalculator scoreCalculator;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int maxResult = request.getMaxResults();

        // <просмотренный id : оценка пользователя>
        Map<Long, Double> userWeights = interactionRepository.findAllByUserId(userId).stream()
                // сначала новые
                .sorted(Comparator.comparing(Interaction::getTimestamp).reversed())
                .limit(Constant.TOP_SIMILARITY_LIMIT)
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating));

        // <кандидат:связанные данные>
        Map<Long, List<VisitedData>> visited = buildAssociations(userWeights.keySet());

        if (visited.isEmpty()) {
            return List.of();
        }

        // <кандидат:оценка>
        Map<Long, Double> candidatesScore = new HashMap<>(visited.size());

        for (Map.Entry<Long, List<VisitedData>> entry : visited.entrySet()) {
            Long candidateId = entry.getKey();
            List<VisitedData> associations = entry.getValue();
            double predictValue = scoreCalculator.calculateScore(candidateId, userWeights, associations);
            candidatesScore.put(candidateId, predictValue);
        }

        Set<Long> recommendationIds = candidatesScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Double::compareTo).reversed())
                .limit(maxResult)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Map<Long, Double> scores = getEventsScores(recommendationIds);

        return recommendationIds.stream()
                .map(id -> buildRecommendedEventProto(id, scores.get(id)))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResult = request.getMaxResults();

        // candidate:similarity
        Map<Long, Similarity> similarities = similarityRepository.findAssociatedByEventIds(Set.of(eventId)).stream()
                .collect(Collectors.toMap(
                        similarity -> eventId == similarity.getEventA() ? similarity.getEventB() : similarity.getEventA(),
                        similarity -> similarity
                ));

        if (similarities.isEmpty()) {
            return List.of();
        }

        // получаем просмотренные id пользователем
        Set<Long> visited = interactionRepository.findAllByUserIdAndEventIdIn(userId, similarities.keySet()).stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        // оставляем только id для ответа
        Set<Long> recommendationsIds = similarities.entrySet().stream()
                .filter(entry -> !visited.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(Similarity::getRating).reversed()))
                .limit(maxResult)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Map<Long, Double> scores = getEventsScores(recommendationsIds);

        return recommendationsIds.stream()
                .map(id -> buildRecommendedEventProto(id, scores.get(id)))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        return interactionRepository.findAllByEventIdIn(eventIds).stream()
                // группировка eventId:сумма рейтингов
                .collect(Collectors.groupingBy(Interaction::getEventId, Collectors.summingDouble(Interaction::getRating)))
                .entrySet().stream()
                .map(entry -> buildRecommendedEventProto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private RecommendedEventProto buildRecommendedEventProto(long eventId, double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }

    /**
     * @param visited - набор посещенных мероприятий
     * @return кандидат:связанность с посещенными мероприятиями
     */
    private Map<Long, List<VisitedData>> buildAssociations(Set<Long> visited) {
        if (visited.isEmpty()) {
            return Map.of();
        }

        // получаем связанные пары
        List<Similarity> associated = similarityRepository.findAssociatedByEventIds(visited);

        if (associated.isEmpty()) {
            return Map.of();
        }

        return associated.stream()
                // исключаем пары, где посещены оба события
                .filter(similarity -> visited.contains(similarity.getEventA()) ^ visited.contains(similarity.getEventB()))
                .sorted(Comparator.comparingDouble(Similarity::getRating).reversed())
                .limit(Constant.TOP_SIMILARITY_LIMIT)
                .map(similarity -> {
                    long visitedId = visited.contains(similarity.getEventA()) ? similarity.getEventA() : similarity.getEventB();
                    long candidateId = similarity.getEventA() == visitedId ? similarity.getEventB() : similarity.getEventA();
                    double similarityScore = similarity.getRating();
                    return new VisitedData(visitedId, candidateId, similarityScore);
                })
                .collect(Collectors.groupingBy(
                        VisitedData::candidateId,
                        Collectors.toList()));
    }

    private Map<Long, Double> getEventsScores(Set<Long> eventsIds) {
        if (eventsIds.isEmpty()) {
            return Map.of();
        }

        return interactionRepository.findAllByEventIdIn(eventsIds).stream()
                .collect(Collectors.groupingBy(
                        Interaction::getEventId,
                        Collectors.averagingDouble(Interaction::getRating)
                ));
    }
}
