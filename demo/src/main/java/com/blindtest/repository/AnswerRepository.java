package com.blindtest.repository;

import com.blindtest.model.Answer;
import com.blindtest.model.Session;
import com.blindtest.model.User;
import com.blindtest.model.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    boolean existsByUserAndSessionAndMusic(User user, Session session, Music music);
    int countBySessionAndMusic(Session session, Music music);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Answer a WHERE a.session.id = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);
}
