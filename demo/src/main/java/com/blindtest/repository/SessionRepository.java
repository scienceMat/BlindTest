package com.blindtest.repository;

import com.blindtest.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.blindtest.model.User;
import java.util.List;
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE :user MEMBER OF s.users")
    List<Session> findFirstSessionByUser(@Param("user") User user);

    @Query("SELECT s FROM Session s LEFT JOIN FETCH s.users")
    List<Session> findAllWithUsers();

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM session_users WHERE session_id = :sessionId", nativeQuery = true)
    void deleteUsersFromSession(@Param("sessionId") Long sessionId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM session_music WHERE session_id = :sessionId", nativeQuery = true)
    void deleteMusicFromSession(@Param("sessionId") Long sessionId);
}
