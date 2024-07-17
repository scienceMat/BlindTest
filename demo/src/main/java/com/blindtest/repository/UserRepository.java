package com.blindtest.repository;

import com.blindtest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN u.sessions s WHERE s.id = :sessionId")
    List<User> findUsersBySessionId(@Param("sessionId") Long sessionId);

    Optional<User> findByName(String name);

}
