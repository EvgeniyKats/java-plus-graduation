package ru.practicum.interaction.dto.event;


import ru.practicum.interaction.dto.comment.GetCommentDto;

import java.util.List;

public interface ResponseEvent {
    void setConfirmedRequests(int confirmedRequests);

    int getConfirmedRequests();

    void setViews(long views);

    long getViews();

    List<GetCommentDto> getComments();

    void setComments(List<GetCommentDto> comments);
}
