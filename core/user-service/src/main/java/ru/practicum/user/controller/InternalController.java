package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.api.user.UserInternalApi;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.logging.Logging;
import ru.practicum.user.service.UserService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/internal/users")
@Validated
@RequiredArgsConstructor
public class InternalController implements UserInternalApi {
    private final UserService userService;

    @Override
    @Logging
    public UserDto findUserById(Long id) {
        return userService.findUserById(id);
    }

    @Override
    @Logging
    public UserShortDto findUserShortById(Long id) {
        return userService.findUserShortById(id);
    }

    @Override
    @Logging
    public Map<Long, UserShortDto> findManyUserShortsByIds(Collection<Long> ids) {
        return userService.findManyUserShortsByIds(ids);
    }
}
