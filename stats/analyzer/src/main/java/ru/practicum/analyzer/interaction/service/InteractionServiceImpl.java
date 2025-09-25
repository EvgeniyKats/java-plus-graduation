package ru.practicum.analyzer.interaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.config.WeightCostByType;
import ru.practicum.analyzer.interaction.mapper.InteractionMapper;
import ru.practicum.analyzer.interaction.model.Interaction;
import ru.practicum.analyzer.interaction.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionRepository interactionRepository;
    private final InteractionMapper interactionMapper;
    private final WeightCostByType weightCostByType;

    @Override
    @Transactional
    public void putAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        Interaction interaction = interactionRepository.findByUserIdAndEventId(userId, eventId)
                .orElseGet(Interaction::new);

        double oldWeight = interaction.getRating();
        double newWeight = weightCostByType.getActionWeight(action.getActionType());

        // при первом действии новый вес будет всегда больше
        if (newWeight > oldWeight) {
            interactionMapper.updateFromUserActionAvro(interaction, action, newWeight);
            interactionRepository.save(interaction);
        }
    }
}
