package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.comment.mapper.MapperComment;
import ru.practicum.comment.model.Comment;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.comment.CommentSortType;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.feign.event.EventInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис не работает напрямую с репозиторием и не содержит транзакции (@Transactional)
 * Для взаимодействия с доменной сущностью он использует CommentTransactionService
 * Работа с внешними сервисами происходит через клиенты Feign
 * Благодаря разделению сокращается длительность и количество соединений с БД
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentTransactionService commentTransactionService;
    private final MapperComment commentMapper;
    private final UserInternalFeign userInternalFeign;
    private final EventInternalFeign eventInternalFeign;

    @Override
    public GetCommentDto addNewComment(Long userId, Long eventId, CommentDto commentDto) {
        UserShortDto commentAuthor = userInternalFeign.findUserShortById(userId);
        EventFullDto event = eventInternalFeign.findById(eventId);
        Comment comment = commentTransactionService.createComment(event, commentDto, userId);
        return commentMapper.toGetCommentDto(comment, commentAuthor);
    }

    @Override
    public GetCommentDto updateComment(Long userId, Long eventId, Long commentId, CommentDto commentDto) {
        UserShortDto commentAuthor = userInternalFeign.findUserShortById(userId);
        Comment comment = commentTransactionService.updateComment(commentDto, commentAuthor, eventId, commentId);
        return commentMapper.toGetCommentDto(comment, commentAuthor);
    }

    @Override
    public void deleteCommentPrivate(Long userId, Long eventId, Long commentId) {
        commentTransactionService.deleteCommentPrivate(userId, eventId, commentId);
    }

    @Override
    public void deleteCommentAdmin(Long eventId, Long commentId) {
        commentTransactionService.deleteCommentAdmin(eventId, commentId);
    }

    @Override
    public GetCommentDto getCommentById(Long eventId, Long commentId) {
        Comment comment = commentTransactionService.getCommentById(eventId, commentId);
        UserShortDto commentAuthor = userInternalFeign.findUserShortById(comment.getAuthorId());
        return commentMapper.toGetCommentDto(comment, commentAuthor);
    }

    @Override
    public List<GetCommentDto> getEventComments(Long eventId, Integer from, Integer size, CommentSortType sortType) {
        Sort sort = switch (sortType) {
            case COMMENTS_OLD -> Sort.by("created").ascending();
            case COMMENTS_NEW -> Sort.by("created").descending();
        };
        Pageable pageable = PageRequest.of(from, size, sort);
        List<Comment> comments = commentTransactionService.getEventComments(eventId, pageable);
        return buildResponseWithAuthors(comments);
    }

    @Override
    public List<GetCommentDto> findByEventId(Long eventId, Pageable pageable) {
        List<Comment> comments = commentTransactionService.getEventComments(eventId, pageable);
        return buildResponseWithAuthors(comments);
    }

    @Override
    public List<GetCommentDto> findLastCommentsForManyEvents(Set<Long> eventIds) {
        List<Comment> comments = commentTransactionService.findLastCommentsForManyEvents(eventIds);
        return buildResponseWithAuthors(comments);
    }

    private List<GetCommentDto> buildResponseWithAuthors(List<Comment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }

        Set<Long> usersIds = comments.stream()
                .map(Comment::getAuthorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userById = userInternalFeign.findManyUserShortsByIds(usersIds);

        return comments.stream()
                .map(comment -> commentMapper.toGetCommentDto(comment, userById.get(comment.getAuthorId())))
                .toList();
    }
}
