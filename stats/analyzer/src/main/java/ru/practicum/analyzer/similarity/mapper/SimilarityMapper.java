package ru.practicum.analyzer.similarity.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.analyzer.similarity.model.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SimilarityMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "eventA", target = "eventA")
    @Mapping(source = "eventB", target = "eventB")
    @Mapping(source = "score", target = "rating")
    @Mapping(source = "timestamp", target = "timestamp")
    void updateEventSimilarityAvro(@MappingTarget Similarity similarity, EventSimilarityAvro eventSimilarityAvro);
}
