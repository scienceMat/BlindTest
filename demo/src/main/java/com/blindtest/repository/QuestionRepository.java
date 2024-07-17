package com.blindtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blindtest.model.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
}