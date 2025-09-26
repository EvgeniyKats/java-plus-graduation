package ru.practicum.analyzer.service.param;

/**
 *
 * @param visitedId идентификатор посещенного мероприятия пользователем
 * @param candidateId идентификатор связанного мероприятия
 * @param similarity связанность пары
 */
public record VisitedData(long visitedId, long candidateId, double similarity) {
}
