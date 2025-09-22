package ru.practicum.interaction.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import ru.practicum.interaction.Constants;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Builder
public class ApiError {

    String message;
    String reason;
    HttpStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_PATTERN)
    LocalDateTime timestamp;
}
