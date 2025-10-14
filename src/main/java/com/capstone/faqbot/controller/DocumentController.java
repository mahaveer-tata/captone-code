package com.capstone.faqbot.controller;

import com.capstone.faqbot.model.DocumentMetadata;
import com.capstone.faqbot.service.RagService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final RagService ragService;

    public DocumentController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/upload", consumes="multipart/form-data")
    public DocumentMetadata uploadDocument(@RequestParam("file")MultipartFile file) throws IOException {
        if(file.isEmpty()){
            throw new IllegalArgumentException("File is empty");
        }

        String content;

        if (file.getOriginalFilename().endsWith(".pdf")) {
            content = extractTextFromPdf(file);
        }
        else {
            content = new String(file.getBytes());
        }

        return ragService.ingestDocument(file.getOriginalFilename(), content);
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())){
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
