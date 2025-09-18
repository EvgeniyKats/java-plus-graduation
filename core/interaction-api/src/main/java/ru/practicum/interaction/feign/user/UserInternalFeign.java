package ru.practicum.interaction.feign.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.user.UserInternalApi;
import ru.practicum.interaction.feign.config.FeignConfig;

@FeignClient(name = "user-service", path = "/internal/users", configuration = FeignConfig.class)
public interface UserInternalFeign extends UserInternalApi {
}
