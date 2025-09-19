package ru.practicum.interaction.feign.comment;

import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.comment.GetCommentDto;

import java.util.List;
import java.util.Set;

@Component
public class CommentInternalFeignFallbackHandler implements CommentInternalFeign {
    // Пустой список комментариев, если сервис недоступен
    @Override
    public List<GetCommentDto> findByEventId(Long eventId, Pageable pageable) {
        return List.of();
    }

    @Override
    public List<GetCommentDto> findLastCommentsForManyEvents(Set<@Positive Long> eventIds) {
        return List.of();
    }
}
