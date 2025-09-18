package ru.practicum.interaction.feign.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.event.EventInternalApi;
import ru.practicum.interaction.feign.config.FeignConfig;

@FeignClient(name = "main-service", path = "/internal/events", configuration = FeignConfig.class)
// TODO: временно используется main-service, изменить при добавлении сервиса event
public interface EventInternalFeign extends EventInternalApi {
}
