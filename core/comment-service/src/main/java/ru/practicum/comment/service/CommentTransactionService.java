package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.model.Comment;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.user.UserShortDto;

import java.util.List;
import java.util.Set;

public interface CommentTransactionService {
    Comment getCommentById(Long eventId, Long commentId);

    List<Comment> getEventComments(Long eventId, Pageable pageable);

    List<Comment> findLastCommentsForManyEvents(Set<Long> eventIds);

    Comment createComment(EventFullDto event, CommentDto commentDto, Long authorId);

    Comment updateComment(CommentDto commentDto, UserShortDto userDto, Long eventId, Long commentId);

    void deleteCommentPrivate(Long userId, Long eventId, Long commentId);

    void deleteCommentAdmin(Long eventId, Long commentId);
}
