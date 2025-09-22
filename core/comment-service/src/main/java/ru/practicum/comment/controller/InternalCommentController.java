package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.service.CommentService;
import ru.practicum.interaction.api.comment.CommentInternalApi;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.logging.Logging;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/internal/comments")
public class InternalCommentController implements CommentInternalApi {
    private final CommentService commentService;


    @Override
    @Logging
    public List<GetCommentDto> findByEventId(Long eventId, Pageable pageable) {
        return commentService.findByEventId(eventId, pageable);
    }

    @Override
    @Logging
    public List<GetCommentDto> findLastCommentsForManyEvents(Set<Long> eventIds) {
        return commentService.findLastCommentsForManyEvents(eventIds);
    }
}
