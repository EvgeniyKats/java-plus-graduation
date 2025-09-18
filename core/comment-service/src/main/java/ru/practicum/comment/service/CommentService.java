package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.comment.CommentSortType;
import ru.practicum.interaction.dto.comment.GetCommentDto;

import java.util.List;
import java.util.Set;

public interface CommentService {
    GetCommentDto addNewComment(Long userId, Long eventId, CommentDto commentDto);

    GetCommentDto updateComment(Long userId, Long eventId, Long commentId, CommentDto commentDto);

    void deleteCommentPrivate(Long userId, Long eventId, Long commentId);

    void deleteCommentAdmin(Long eventId, Long commentId);

    GetCommentDto getCommentById(Long eventId, Long commentId);

    List<GetCommentDto> getEventComments(Long eventId, Integer from, Integer size, CommentSortType sort);

    List<GetCommentDto> findByEventId(Long eventId, Pageable pageable);

    List<GetCommentDto> findLastCommentsForManyEvents(Set<Long> eventIds);
}
