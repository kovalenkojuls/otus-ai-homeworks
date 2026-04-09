package com.example.questionnaire.service;

import com.example.questionnaire.model.Question;
import com.example.questionnaire.model.Answer;
import com.example.questionnaire.repository.QuestionRepository;
import com.example.questionnaire.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public List<Question> getQuestions() {
        return questionRepository.findAll();
    }

    public void saveAnswer(Answer answer) {
        answer.setUserId(1);
        answerRepository.save(answer);
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }
}
