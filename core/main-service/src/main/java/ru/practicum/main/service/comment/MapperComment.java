package ru.practicum.main.service.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.comment.GetCommentDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.main.service.comment.model.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperComment {

    Comment toComment(CommentDto commentDto);

    @Mapping(source = "comment.id", target = "id")
    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    @Mapping(source = "userShortDto", target = "author")
    GetCommentDto toGetCommentDto(Comment comment, UserShortDto userShortDto);
}
