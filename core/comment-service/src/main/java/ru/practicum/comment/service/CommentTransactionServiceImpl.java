package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.mapper.MapperComment;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.interaction.Constants;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static ru.practicum.interaction.dto.event.EventState.PUBLISHED;

/**
 * Все методы выполняются в транзакции целиком (@Transactional)
 * Использует только репозитории и мапперы
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentTransactionServiceImpl implements CommentTransactionService {
    private final MapperComment commentMapper;
    private final CommentRepository commentRepository;

    @Override
    public Comment getCommentById(Long eventId, Long commentId) {
        Comment commentFromDb = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!commentFromDb.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        return commentFromDb;
    }

    @Override
    public List<Comment> getEventComments(Long eventId, Pageable pageable) {
        return commentRepository.findByEventId(eventId, pageable);
    }

    @Override
    public List<Comment> findLastCommentsForManyEvents(Set<Long> eventIds) {
        return commentRepository.findLastCommentsForManyEvents(eventIds);
    }

    @Override
    @Transactional
    public Comment createComment(EventFullDto event, CommentDto commentDto, Long authorId) {
        if (!event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Событие ещё не опубликовано eventId=" + event.getId());
        }

        Comment comment = commentMapper.toComment(commentDto, authorId, event.getId());
        commentRepository.save(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment updateComment(CommentDto commentDto, UserShortDto userDto, Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));

        if (!comment.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        if (!comment.getAuthorId().equals(userDto.getId())) {
            throw new NotFoundException(Constants.COMMENT_AUTHOR_NOT_MATCH);
        }
        if (comment.getCreated().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new ConflictException("Комментарий может быть изменен только в первые 24 часа после создания");
        }

        comment.setText(commentDto.getText());

        return comment;
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long userId, Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!comment.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        if (!comment.getAuthorId().equals(userId)) {
            throw new NotFoundException(Constants.COMMENT_AUTHOR_NOT_MATCH);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!comment.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        commentRepository.deleteById(commentId);
    }
}
