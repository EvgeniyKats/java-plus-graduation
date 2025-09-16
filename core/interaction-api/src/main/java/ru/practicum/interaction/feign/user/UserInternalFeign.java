package ru.practicum.interaction.feign.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.user.UserInternalApi;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserInternalFeign extends UserInternalApi {
}
