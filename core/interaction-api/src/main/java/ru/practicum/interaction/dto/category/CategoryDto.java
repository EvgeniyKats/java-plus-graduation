package ru.practicum.interaction.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.interaction.validator.SizeAfterTrim;

@Getter
@Setter
public class CategoryDto {
    @Min(1)
    private Long id;

    @NotBlank
    @SizeAfterTrim(min = 1, max = 50)
    private String name;
}
