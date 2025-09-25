package ru.practicum.analyzer.similarity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.similarity.mapper.SimilarityMapper;
import ru.practicum.analyzer.similarity.model.Similarity;
import ru.practicum.analyzer.similarity.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {
    private final SimilarityRepository similarityRepository;
    private final SimilarityMapper similarityMapper;

    @Override
    @Transactional
    public void putSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        long eventA = eventSimilarityAvro.getEventA();
        long eventB = eventSimilarityAvro.getEventB();
        Similarity similarity = similarityRepository.findByEventAAndEventB(eventA, eventB)
                .orElseGet(Similarity::new);
        similarityMapper.updateEventSimilarityAvro(similarity, eventSimilarityAvro);
        similarityRepository.save(similarity);
    }
}
