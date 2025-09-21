package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.mapper.MapperComment;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.interaction.Constants;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.comment.CommentSortType;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.feign.event.EventInternalFeign;
import ru.practicum.interaction.feign.user.UserInternalFeign;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.interaction.dto.event.EventState.PUBLISHED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MapperComment commentMapper;
    private final UserInternalFeign userInternalFeign;
    private final EventInternalFeign eventInternalFeign;

    @Override
    @Transactional
    public GetCommentDto addNewComment(Long userId, Long eventId, CommentDto commentDto) {
        UserShortDto commentAuthor = userInternalFeign.findUserShortById(userId);

        EventFullDto event = eventInternalFeign.findById(eventId);

        if (!event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Событие ещё не опубликовано eventId=" + eventId);
        }

        Comment comment = commentMapper.toComment(commentDto, commentAuthor.getId(), event.getId());
        commentRepository.save(comment);

        return commentMapper.toGetCommentDto(comment, commentAuthor);
    }

    @Override
    @Transactional
    public GetCommentDto updateComment(Long userId, Long eventId, Long commentId, CommentDto commentDto) {
        Comment commentFromDb = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));

        if (!commentFromDb.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        if (!commentFromDb.getAuthorId().equals(userId)) {
            throw new NotFoundException(Constants.COMMENT_AUTHOR_NOT_MATCH);
        }
        if (commentFromDb.getCreated().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new ConflictException("Комментарий может быть изменен только в первые 24 часа после создания");
        }
        commentFromDb.setText(commentDto.getText());
        UserShortDto commentAuthor = userInternalFeign.findUserShortById(userId);

        return commentMapper.toGetCommentDto(commentFromDb, commentAuthor);
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long userId, Long eventId, Long commentId) {
        Comment commentFromDb = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!commentFromDb.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        if (!commentFromDb.getAuthorId().equals(userId)) {
            throw new NotFoundException(Constants.COMMENT_AUTHOR_NOT_MATCH);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long eventId, Long commentId) {
        Comment commentFromDb = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!commentFromDb.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public GetCommentDto getCommentById(Long eventId, Long commentId) {
        Comment commentFromDb = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Constants.COMMENT_NOT_FOUND));
        if (!commentFromDb.getEventId().equals(eventId)) {
            throw new NotFoundException(Constants.COMMENT_EVENT_NOT_MATCH);
        }
        UserShortDto commentAuthor = userInternalFeign.findUserShortById(commentFromDb.getAuthorId());
        return commentMapper.toGetCommentDto(commentFromDb, commentAuthor);
    }

    @Override
    public List<GetCommentDto> getEventComments(Long eventId, Integer from, Integer size, CommentSortType sortType) {
        Sort sort = switch (sortType) {
            case COMMENTS_OLD -> Sort.by("created").ascending();
            case COMMENTS_NEW -> Sort.by("created").descending();
        };
        Pageable pageable = PageRequest.of(from, size, sort);
        List<Comment> comments = commentRepository.findByEventId(eventId, pageable);
        return buildResponseWithAuthors(comments);
    }

    @Override
    public List<GetCommentDto> findByEventId(Long eventId, Pageable pageable) {
        List<Comment> comments = commentRepository.findByEventId(eventId, pageable);
        return buildResponseWithAuthors(comments);
    }

    @Override
    public List<GetCommentDto> findLastCommentsForManyEvents(Set<Long> eventIds) {
        List<Comment> comments = commentRepository.findLastCommentsForManyEvents(eventIds);
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
