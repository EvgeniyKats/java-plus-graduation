package ru.practicum.interaction.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.interaction.feign.ServiceUnavailableException;
import ru.practicum.logging.Logging;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @Logging(Level.WARN)
    public ApiError handleServiceUnavailable(final ServiceUnavailableException e) {
        return buildResponseByServiceUnavailable(e);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Logging(Level.WARN)
    public ApiError handleBadRequest(final BadRequestException e) {
        return e.getError();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Logging(Level.WARN)
    public ApiError handleAddLikeException(final AddLikeException e) {
        return e.getError();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Logging(Level.WARN)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        return ApiError.builder()
                .reason("Нарушение валидации (constraint)")
                .message("parameter=" + e.getConstraintViolations().toString())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Logging(Level.WARN)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return ApiError.builder()
                .reason("Нарушение валидации (method)")
                .message("parameter=" + e.getParameter().getParameterName())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Logging(Level.WARN)
    public ApiError handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        return ApiError.builder()
                .reason("Не был передан необходимый параметр")
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @Logging(Level.WARN)
    public ApiError handleNotFound(final NotFoundException e) {
        return e.getError();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @Logging(Level.WARN)
    public ApiError handlerDuplicate(final DuplicateException e) {
        return e.getError();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @Logging(Level.WARN)
    public ApiError handleConflict(final ConflictException e) {
        return e.getError();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Logging(Level.ERROR)
    public ApiError handleInternalServerError(InternalServerException e) {
        return e.getError();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Logging(Level.ERROR)
    public ApiError handleOtherError(Exception e) {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(e.getMessage())
                .reason("Unknown internal error.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private ApiError buildResponseByServiceUnavailable(ServiceUnavailableException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Service unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
