package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserShort;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperUser {
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(UserShort userShort);
}
