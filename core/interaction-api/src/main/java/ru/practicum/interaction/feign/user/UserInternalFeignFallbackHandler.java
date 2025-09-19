package ru.practicum.interaction.feign.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserInternalFeignFallbackHandler implements UserInternalFeign {
    private final UserFallbackException userFallbackException;

    @Override
    public UserDto findUserById(Long id) {
        throw userFallbackException;
    }

    @Override
    public UserShortDto findUserShortById(Long id) {
        throw userFallbackException;
    }

    @Override
    public Map<Long, UserShortDto> findManyUserShortsByIds(Collection<Long> ids) {
        throw userFallbackException;
    }
}
