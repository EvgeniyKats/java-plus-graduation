package ru.practicum.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.interaction.processor.InteractionProcessor;
import ru.practicum.analyzer.similarity.processor.SimilarityProcessor;

/**
 * Запускает выполнение процессов
 */
@Component
@RequiredArgsConstructor
public class AnalyzerStarter implements CommandLineRunner {
    private final SimilarityProcessor similarityProcessor;
    private final InteractionProcessor interactionProcessor;

    @Override
    public void run(String... args) {
        Thread similarityThread = new Thread(similarityProcessor);
        similarityThread.setName("similarityThread");
        similarityThread.start();

        Thread interactionThread = new Thread(interactionProcessor);
        interactionThread.setName("InteractionThread");
        interactionThread.start();
    }
}
