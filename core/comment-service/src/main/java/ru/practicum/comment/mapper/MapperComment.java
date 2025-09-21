package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.comment.model.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperComment {
    @Mapping(source = "authorId", target = "authorId")
    @Mapping(source = "eventId", target = "eventId")
    Comment toComment(CommentDto commentDto, Long authorId, Long eventId);

    @Mapping(source = "comment.id", target = "id")
    @Mapping(source = "userShortDto", target = "author")
    GetCommentDto toGetCommentDto(Comment comment, UserShortDto userShortDto);
}
