package ru.practicum.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserShort;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u " +
            " FROM User u WHERE (:ids is NULL OR u.id IN :ids)")
    Page<User> findUsersByIds(List<Long> ids, Pageable pageable);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT u.id as id, u.name as name " +
           "FROM User u " +
           "WHERE u.id = :id")
    Optional<UserShort> findOneUserShortById(@Param("id") Long id);

    @Query("SELECT u.id as id, u.name as name " +
           "FROM User u " +
           "WHERE u.id IN :ids")
    Collection<UserShort> findManyUserShortByIds(@Param("ids") Collection<Long> ids);
}
