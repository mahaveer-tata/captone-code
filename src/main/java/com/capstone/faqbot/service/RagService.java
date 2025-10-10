package com.capstone.faqbot.service;

import com.capstone.faqbot.model.AnswerResponse;
import com.capstone.faqbot.model.DocumentMetadata;
import com.capstone.faqbot.model.HistoryResponse;
import com.capstone.faqbot.model.QuestionRequest;
import com.capstone.faqbot.repo.HistoryRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final OpenAiChatModel openAiChatModel;
    private final HistoryRepository repo;

    public RagService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, OpenAiChatModel openAiChatModel, HistoryRepository repo) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.openAiChatModel = openAiChatModel;
        this.repo = repo;
    }

    public DocumentMetadata ingestDocument(String filename, String content) {
        int chunkSize = 500;
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < content.length(); i+= chunkSize) {
            int end = Math.min(content.length(), i + chunkSize);
            chunks.add(content.substring(i, end));
        }

        for (String chunk : chunks) {
            TextSegment segment = TextSegment.from(chunk);
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }
        return new DocumentMetadata(filename, String.valueOf(content.length()));
    }

    public AnswerResponse answerResponse(QuestionRequest questionRequest) {
        String question = questionRequest.question();

        Embedding queryEmbedding = embeddingModel.embed(question).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .build();

        List<String> chunks = embeddingStore.search(searchRequest)
                .matches()
                .stream()
                .map(m -> m.embedded().text())
                .collect(Collectors.toList());

        String context = String.join("\n", chunks);

        String prompt = "Answer the following question using the context below in plain text and result should be only from the uploaded pdf. "
                + "Do not use any markdown, bold, or special characters.\n\nContext:\n"
                + context + "\n\nQuestion: " + question + "\nAnswer:";

        String answer = openAiChatModel.generate(prompt);
        answer = answer.replace("\\n", "\n").trim();

        answer = answer.replaceAll("^\\*+\\s*", "");

        List<String> answerLines = List.of(answer.split("\\r?\\n"));

        HistoryResponse history = new HistoryResponse();
        history.setQuestion(questionRequest.question());
        history.setAnswer(String.join("\n", answerLines));
        history.setCreatedAt(LocalDateTime.now());
        repo.save(history);

        return new AnswerResponse(question, answerLines);
    }
}
