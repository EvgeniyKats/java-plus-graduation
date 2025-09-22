package ru.practicum.interaction.api.user;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.Collection;
import java.util.Map;

public interface UserInternalApi {

    @GetMapping("/{id}/full")
    UserDto findUserById(@PathVariable @Positive Long id);

    @GetMapping("/{id}/short")
    UserShortDto findUserShortById(@PathVariable @Positive Long id);

    @GetMapping
    Map<Long, UserShortDto> findManyUserShortsByIds(@RequestParam Collection<@Positive Long> ids);
}
