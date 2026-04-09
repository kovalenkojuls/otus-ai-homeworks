package com.example.questionnaire.controller;

import com.example.questionnaire.model.Question;
import com.example.questionnaire.model.Answer;
import com.example.questionnaire.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionnaireRestController {
    private final QuestionnaireService questionnaireService;

    @GetMapping("/questions")
    public List<Question> getQuestions() {
        return questionnaireService.getQuestions();
    }

    @PostMapping("/answers")
    public void submitAnswers(@RequestBody Map<String, Object> payload) {
        List<String> answers = (List<String>) payload.get("answers");
        if (answers == null) return;
        for (int i = 0; i < answers.size(); i++) {
            Answer answer = new Answer();
            answer.setUserId(1);
            answer.setQuestionId(i + 1);
            answer.setAnswerText(answers.get(i));
            questionnaireService.saveAnswer(answer);
        }
    }
}
