package com.capstone.faqbot.controller;

import com.capstone.faqbot.model.AnswerResponse;
import com.capstone.faqbot.model.QuestionRequest;
import com.capstone.faqbot.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ask")

public class FaqController {
    private final RagService ragService;


    public FaqController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    public AnswerResponse askQuestion(@RequestBody QuestionRequest questionRequest) {
        return ragService.answerResponse(questionRequest);
    }
}
