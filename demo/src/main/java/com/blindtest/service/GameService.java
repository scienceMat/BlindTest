package com.blindtest.service;

import com.blindtest.model.Question;
import com.blindtest.model.User;
import com.blindtest.repository.QuestionRepository;
import com.blindtest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public void submitAnswer(Long userId, Long questionId, String answer) {
        User user = userRepository.findById(userId).orElseThrow();
        Question question = questionRepository.findById(questionId).orElseThrow();

        if (question.getCorrectAnswer().equals(answer)) {
            user.setScore(user.getScore() + 1);
            userRepository.save(user);
        }
    }

    public boolean submitAnswer(Long userId, Long questionId, String title, String artist) {
        User user = userRepository.findById(userId).orElseThrow();
        Question question = questionRepository.findById(questionId).orElseThrow();

        if (question.getTitle().equalsIgnoreCase(title) && question.getArtist().equalsIgnoreCase(artist)) {
            user.setScore(user.getScore() + 1);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
