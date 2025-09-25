package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.interaction.model.Interaction;
import ru.practicum.analyzer.interaction.repository.InteractionRepository;
import ru.practicum.analyzer.similarity.model.Similarity;
import ru.practicum.analyzer.similarity.repository.SimilarityRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

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

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int maxResult = request.getMaxResults();

        // получаем просмотренные id пользователем
        Set<Long> visited = interactionRepository.findAllByUserId(userId).stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        return buildAssociated(visited).entrySet().stream()
                .filter(entry -> !visited.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResult)
                .map(entry -> buildRecommendedEventProto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResult = request.getMaxResults();

        Map<Long, Double> eventRatings = buildAssociated(Set.of(eventId));

        // получаем просмотренные id пользователем
        Set<Long> visited = interactionRepository.findAllByUserIdAndEventIdIn(userId, eventRatings.keySet()).stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        return eventRatings.entrySet().stream()
                .filter(entry -> !visited.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResult)
                .map(entry -> buildRecommendedEventProto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();

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
     * @param visited - набор посещенных мероприятий, для которых будут искаться ассоциации
     * @return мапа связанный eventId : рейтинг пары
     */
    private Map<Long, Double> buildAssociated(Set<Long> visited) {
        // получаем связанные пары
        List<Similarity> associated = similarityRepository.findAssociatedByEventIds(visited);

        // связанный eventId : рейтинг пары с текущим
        return associated.stream()
                .collect(Collectors.toMap(
                        similarity -> visited.contains(similarity.getEventA()) ? similarity.getEventB() : similarity.getEventA(),
                        Similarity::getRating));
    }
}
