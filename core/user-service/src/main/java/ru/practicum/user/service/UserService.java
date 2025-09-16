package ru.practicum.user.service;


import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserService {
    List<UserDto> getUsers(GetUserParam getUserParam);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    UserDto findUserById(Long id);

    UserShortDto findUserShortById(Long id);

    Map<Long, UserShortDto> findManyUserShortsByIds(Collection<Long> ids);
}
