package ru.practicum.interaction.feign.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.request.RequestInternalApi;
import ru.practicum.interaction.feign.config.FeignConfig;

@FeignClient(name = "request-service",
        path = "/internal/requests",
        configuration = FeignConfig.class,
        fallback = RequestInternalFeignFallbackHandler.class)
public interface RequestInternalFeign extends RequestInternalApi {
}
