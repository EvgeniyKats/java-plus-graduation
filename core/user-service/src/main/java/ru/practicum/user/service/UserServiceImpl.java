package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.Constants;
import ru.practicum.interaction.dto.user.NewUserRequest;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.exception.DuplicateException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.user.mapper.MapperUser;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserShort;
import ru.practicum.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MapperUser mapperUser;

    @Override
    public List<UserDto> getUsers(GetUserParam getUserParam) {
        Page<User> users = userRepository.findUsersByIds(getUserParam.getIds(), getUserParam.getPageable());
        return users.map(mapperUser::toUserDto).getContent();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        if (userRepository.findByEmailIgnoreCase(newUserRequest.getEmail()).isPresent()) {
            throw new DuplicateException(Constants.DUPLICATE_USER);
        }

        User user = mapperUser.toUser(newUserRequest);
        userRepository.save(user);
        return mapperUser.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(Constants.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }

    @Override
    public UserDto findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Constants.USER_NOT_FOUND));

        return mapperUser.toUserDto(user);
    }

    @Override
    public UserShortDto findUserShortById(Long id) {
        UserShort userShort = userRepository.findOneUserShortById(id)
                .orElseThrow(() -> new NotFoundException(Constants.USER_NOT_FOUND));

        return mapperUser.toUserShortDto(userShort);
    }

    @Override
    public Map<Long, UserShortDto> findManyUserShortsByIds(Collection<Long> ids) {
        Collection<UserShort> userShorts = userRepository.findManyUserShortByIds(ids);

        if (userShorts.size() < ids.size()) {
            throw new NotFoundException(Constants.USER_NOT_FOUND);
        }

        return userShorts.stream()
                .collect(Collectors.toMap(UserShort::getId, mapperUser::toUserShortDto));
    }
}
