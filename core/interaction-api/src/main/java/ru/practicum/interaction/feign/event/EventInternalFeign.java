package ru.practicum.interaction.feign.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.event.EventInternalApi;
import ru.practicum.interaction.feign.config.FeignConfig;

@FeignClient(name = "event-service", path = "/internal/events", configuration = FeignConfig.class)
public interface EventInternalFeign extends EventInternalApi {
}
