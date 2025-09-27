package ru.practicum.analyzer.interaction.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.analyzer.interaction.model.Interaction;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InteractionMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userActionAvro.userId", target = "userId")
    @Mapping(source = "userActionAvro.eventId", target = "eventId")
    @Mapping(source = "newWeight", target = "rating")
    @Mapping(source = "userActionAvro.timestamp", target = "timestamp")
    void updateFromUserActionAvro(@MappingTarget Interaction interaction, UserActionAvro userActionAvro, double newWeight);
}
