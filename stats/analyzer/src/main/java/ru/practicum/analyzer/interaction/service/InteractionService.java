package ru.practicum.analyzer.interaction.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface InteractionService {
    void putAction(UserActionAvro action);
}
