package ru.practicum.interaction.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserShortDto {
    @Min(1)
    private Long id;

    @NotBlank
    private String name;
}
