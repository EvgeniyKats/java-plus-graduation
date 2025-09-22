package ru.practicum.interaction.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.InternalServerException;
import ru.practicum.interaction.exception.NotFoundException;

import java.io.IOException;

public class FeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String msg = "Body empty";
        try {
            if (response.body() != null) {
                byte[] bodyBytes = Util.toByteArray(response.body().asInputStream());
                JsonNode jsonNode = objectMapper.readTree(bodyBytes);
                msg = jsonNode.findValue("message").asText();
            }
        } catch (IOException e) {
            msg = "Failed to read body";
        }

        switch (response.status()) {
            case 400 -> {
                return new BadRequestException(msg);
            }
            // обработка ошибки 404 (Not Found)
            case 404 -> {
                return new NotFoundException(msg);
            }
            case 409 -> {
                return new ConflictException(msg);
            }
            // обработка ошибки 500 (Internal Server Error)
            case 500 -> {
                return new InternalServerException("Server error occurred");
            }
            case 503 -> {
                return new ServiceUnavailableException(msg);
            }
            // для других кодов используем стандартное поведение
            default -> {
                return defaultDecoder.decode(methodKey, response);
            }
        }
    }
}
