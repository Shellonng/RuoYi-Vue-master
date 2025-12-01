package com.exampl.smartcourse.service.aiGrading;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileContentReader {

    public static String readFileContent(String filePath) throws IOException {
        if (filePath == null) throw new IOException("filePath is null");
        Path path = Path.of(filePath);
        if (!Files.exists(path)) throw new IOException("file not found: " + filePath);
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return readPdf(path.toFile());
        } else if (lower.endsWith(".docx")) {
            return readDocx(path.toFile());
        } else if (lower.endsWith(".doc")) {
            return readDoc(path.toFile());
        } else if (lower.endsWith(".txt")) {
            return Files.readString(path, StandardCharsets.UTF_8);
        } else {
            // Fallback: try reading as text
            return Files.readString(path, StandardCharsets.UTF_8);
        }
    }

    private static String readPdf(File file) throws IOException {
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private static String readDoc(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis)) {
            WordExtractor extractor = new WordExtractor(doc);
            String[] paragraphs = extractor.getParagraphText();
            return String.join("\n", paragraphs).trim();
        }
    }

    private static String readDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis)) {
            List<XWPFParagraph> paragraphs = docx.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }
}