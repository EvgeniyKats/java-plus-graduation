package ru.practicum.event.event;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.event.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
