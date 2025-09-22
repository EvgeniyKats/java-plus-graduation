package ru.practicum.event.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsByNameIgnoreCase(String name);
}
