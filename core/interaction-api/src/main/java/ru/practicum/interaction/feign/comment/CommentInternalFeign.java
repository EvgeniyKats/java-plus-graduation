package ru.practicum.interaction.feign.comment;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.comment.CommentInternalApi;
import ru.practicum.interaction.feign.config.FeignConfig;

@FeignClient(name = "comment-service",
        path = "/internal/comments",
        configuration = FeignConfig.class,
        fallback = CommentInternalFeignFallbackHandler.class)
public interface CommentInternalFeign extends CommentInternalApi {
}
