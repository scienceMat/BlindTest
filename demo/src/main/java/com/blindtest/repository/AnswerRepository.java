package com.blindtest.repository;

import com.blindtest.model.Answer;
import com.blindtest.model.Session;
import com.blindtest.model.User;
import com.blindtest.model.Music;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    boolean existsByUserAndSessionAndMusic(User user, Session session, Music music);
    int countBySessionAndMusic(Session session, Music music);
    void deleteAllBySession(Session session);
}
