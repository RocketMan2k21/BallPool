package com.example.parallel.lab4.src.main.java.part4;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ITDocumentSearcherTest {
    private static final String TEST_DIR = "test_documents";
    private Path testDirPath;

    @BeforeEach
    void setUp() throws IOException {
        testDirPath = Paths.get(System.getProperty("user.dir"), TEST_DIR);
        Files.createDirectories(testDirPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testDirPath)) {
            Files.walk(testDirPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Test
    void testHighlyRelevantDocument() throws IOException {
        String content = "This document is about software development and programming. " +
                "It discusses Java, Python, and algorithms. The document also covers " +
                "databases, SQL, and web development. Machine learning and artificial " +
                "intelligence are important topics in modern software engineering.";
        
        createTestFile("tech_doc.txt", content);
        
        List<Path> documents = getTestDocuments();
        List<ITDocumentSearcher.SearchResult> results = ITDocumentSearcher.searchDocuments(documents, 2);
        
        assertFalse(results.isEmpty(), "Should find relevant document");
        ITDocumentSearcher.SearchResult result = results.get(0);
        assertTrue(result.getRelevanceScore() > 0.2, "Should have high relevance score");
        assertTrue(result.getMatchedKeywords().contains("software"), "Should match 'software'");
        assertTrue(result.getMatchedKeywords().contains("java"), "Should match 'java'");
    }

    @Test
    void testNonRelevantDocument() throws IOException {
        String content = "This document is about cooking and recipes. " +
                "It discusses various ingredients and cooking techniques. " +
                "The document includes recipes for different dishes.";
        
        createTestFile("cooking_doc.txt", content);
        
        List<Path> documents = getTestDocuments();
        List<ITDocumentSearcher.SearchResult> results = ITDocumentSearcher.searchDocuments(documents, 2);
        
        assertTrue(results.isEmpty() || results.get(0).getRelevanceScore() < 0.1,
                "Should have very low or zero relevance score");
    }

    @Test
    void testMultipleDocuments() throws IOException {
        String techContent = "Programming in Java and Python. Web development and databases.";
        String mixedContent = "Some programming concepts, but mainly about history and art.";
        String nonTechContent = "A document about geography and natural history.";
        
        createTestFile("tech.txt", techContent);
        createTestFile("mixed.txt", mixedContent);
        createTestFile("nontech.txt", nonTechContent);
        
        List<Path> documents = getTestDocuments();
        List<ITDocumentSearcher.SearchResult> results = ITDocumentSearcher.searchDocuments(documents, 2);
        
        assertEquals(2, results.stream()
                .filter(r -> r.getRelevanceScore() > 0)
                .count(), "Should find two documents with some relevance");
        
        assertTrue(results.get(0).getRelevanceScore() > results.get(1).getRelevanceScore(),
                "Results should be sorted by relevance");
    }

    @Test
    void testParallelPerformance() throws IOException {
        // Create multiple documents
        for (int i = 0; i < 100; i++) {
            String content = i % 2 == 0 ?
                    "Technical document about programming and software development." :
                    "Non-technical document about various topics.";
            createTestFile("doc" + i + ".txt", content);
        }

        List<Path> documents = getTestDocuments();
        
        // Compare execution time with different thread counts
        long singleThreadTime = measureExecutionTime(documents, 1);
        long multiThreadTime = measureExecutionTime(documents, Runtime.getRuntime().availableProcessors());
        
        assertTrue(multiThreadTime < singleThreadTime,
                "Multi-threaded execution should be faster than single-threaded");
    }

    private long measureExecutionTime(List<Path> documents, int threadCount) {
        long startTime = System.nanoTime();
        ITDocumentSearcher.searchDocuments(documents, threadCount);
        return System.nanoTime() - startTime;
    }

    private void createTestFile(String filename, String content) throws IOException {
        Path filePath = testDirPath.resolve(filename);
        Files.write(filePath, content.getBytes());
    }

    private List<Path> getTestDocuments() throws IOException {
        return Files.walk(testDirPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .collect(Collectors.toList());
    }
} 