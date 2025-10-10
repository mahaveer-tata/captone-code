package com.capstone.faqbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentMetadata {
    private String fileName;
    private String status;
}
