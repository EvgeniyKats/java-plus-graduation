package ru.practicum.collector.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.interaction.exception.InternalServerException;

import java.time.Instant;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserActionMapper {
    UserActionAvro toUserActionAvro(UserActionProto userActionProto);

    default ActionTypeAvro toActionTypeAvro(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> throw new InternalServerException("Unknown type ActionTypeProto:" + actionTypeProto);
        };
    }

    default Instant toInstant(Timestamp timestamp) {
        long seconds = timestamp.getSeconds();
        long nanos = timestamp.getNanos();
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
