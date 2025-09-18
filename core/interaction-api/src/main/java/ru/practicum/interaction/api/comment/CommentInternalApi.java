package ru.practicum.interaction.api.comment;

import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.comment.GetCommentDto;

import java.util.List;
import java.util.Set;

public interface CommentInternalApi {
    @GetMapping("/{eventId}")
    List<GetCommentDto> findByEventId(@PathVariable @Positive Long eventId,
                                      @PageableDefault(
                                              sort = "created",
                                              direction = Sort.Direction.DESC
                                      ) Pageable pageable);

    @GetMapping
    List<GetCommentDto> findLastCommentsForManyEvents(@RequestParam Set<@Positive Long> eventIds);
}
