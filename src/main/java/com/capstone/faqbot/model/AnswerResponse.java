package com.capstone.faqbot.model;

import java.util.List;

public record AnswerResponse(
        String question,
        List<String> answers
) {}
