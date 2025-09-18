package ru.practicum.main.service.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto updateById(Long catId, CategoryDto categoryDto);

    void deleteById(Long catId);

    CategoryDto getById(Long catId);

    List<CategoryDto> getAll(Pageable pageable);

}
