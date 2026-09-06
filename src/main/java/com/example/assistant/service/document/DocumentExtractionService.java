package com.example.assistant.service.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class DocumentExtractionService {

    private static final int MAX_DOCUMENT_CHARS = 30000; // Limit extracted text size for context window

    public String extractText(InputStream inputStream, String fileName) {
        if (fileName == null) {
            fileName = "document.txt";
        }

        String lower = fileName.toLowerCase();
        try {
            if (lower.endsWith(".pdf")) {
                return extractPdfText(inputStream);
            } else if (lower.endsWith(".docx")) {
                return extractDocxText(inputStream);
            } else {
                return extractPlainText(inputStream);
            }
        } catch (Exception e) {
            log.error("Failed to extract text from document {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Could not read document contents: " + e.getMessage(), e);
        }
    }

    private String extractPdfText(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return truncateIfNeeded(text);
        }
    }

    private String extractDocxText(InputStream inputStream) throws Exception {
        try (XWPFDocument docx = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return truncateIfNeeded(extractor.getText());
        }
    }

    private String extractPlainText(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        String text = new String(bytes, StandardCharsets.UTF_8);
        return truncateIfNeeded(text);
    }

    private String truncateIfNeeded(String text) {
        if (text == null) return "";
        if (text.length() <= MAX_DOCUMENT_CHARS) {
            return text.trim();
        }
        return text.substring(0, MAX_DOCUMENT_CHARS) + "\n\n[...Document truncated for AI context...]";
    }
}
