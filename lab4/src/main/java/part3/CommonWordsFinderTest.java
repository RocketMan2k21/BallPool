package com.example.parallel.lab4.src.main.java.part3;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CommonWordsFinderTest {
    private static final String TEST_DIR = "test_documents";
    private Path testDirPath;

    @BeforeEach
    void setUp() throws IOException {
        // Create test directory
        testDirPath = Paths.get(TEST_DIR);
        Files.createDirectories(testDirPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
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
    void testEmptyFiles() throws IOException {
        // Create empty files
        createTestFile("file1.txt", "");
        createTestFile("file2.txt", "");

        List<Path> documents = getTestDocuments();
        Set<String> commonWords = CommonWordsFinder.findCommonWords(documents, 2);

        assertTrue(commonWords.isEmpty(), "Empty files should result in empty common words set");
    }

    @Test
    void testIdenticalFiles() throws IOException {
        String content = "apple banana cherry date";
        createTestFile("file1.txt", content);
        createTestFile("file2.txt", content);

        List<Path> documents = getTestDocuments();
        Set<String> commonWords = CommonWordsFinder.findCommonWords(documents, 2);

        Set<String> expected = new HashSet<>(Arrays.asList("apple", "banana", "cherry", "date"));
        assertEquals(expected, commonWords, "Identical files should have all words in common");
    }

    @Test
    void testPartiallyOverlappingFiles() throws IOException {
        createTestFile("file1.txt", "apple banana cherry");
        createTestFile("file2.txt", "banana cherry date");
        createTestFile("file3.txt", "cherry date apple");

        List<Path> documents = getTestDocuments();
        Set<String> commonWords = CommonWordsFinder.findCommonWords(documents, 2);

        Set<String> expected = new HashSet<>(Collections.singletonList("cherry"));
        assertEquals(expected, commonWords, "Should find only words common to all files");
    }

    @Test
    void testCaseInsensitivity() throws IOException {
        createTestFile("file1.txt", "Apple BANANA Cherry");
        createTestFile("file2.txt", "apple Banana CHERRY");

        List<Path> documents = getTestDocuments();
        Set<String> commonWords = CommonWordsFinder.findCommonWords(documents, 2);

        Set<String> expected = new HashSet<>(Arrays.asList("apple", "banana", "cherry"));
        assertEquals(expected, commonWords, "Should be case insensitive");
    }

    @Test
    void testPunctuation() throws IOException {
        createTestFile("file1.txt", "apple, banana! cherry?");
        createTestFile("file2.txt", "apple. banana; cherry:");

        List<Path> documents = getTestDocuments();
        Set<String> commonWords = CommonWordsFinder.findCommonWords(documents, 2);

        Set<String> expected = new HashSet<>(Arrays.asList("apple", "banana", "cherry"));
        assertEquals(expected, commonWords, "Should ignore punctuation");
    }

    @Test
    void testMultipleThreads() throws IOException {
        // Create larger files to test parallel processing
        StringBuilder content1 = new StringBuilder();
        StringBuilder content2 = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            content1.append("apple banana cherry date ").append(i).append(" ");
            content2.append("apple banana cherry date ").append(i).append(" ");
        }

        createTestFile("file1.txt", content1.toString());
        createTestFile("file2.txt", content2.toString());

        List<Path> documents = getTestDocuments();
        
        // Test with different thread counts
        int[] threadCounts = {1, 2, 4, 8};
        Set<String> previousResult = null;

        for (int threadCount : threadCounts) {
            Set<String> result = CommonWordsFinder.findCommonWords(documents, threadCount);
            
            if (previousResult != null) {
                assertEquals(previousResult, result, 
                    "Results should be consistent across different thread counts");
            }
            
            previousResult = result;
            assertTrue(result.containsAll(Arrays.asList("apple", "banana", "cherry", "date")),
                    "Common words should be found regardless of thread count");
        }
    }

    @Test
    void testNonExistentFiles() {
        List<Path> documents = Arrays.asList(
            Paths.get("nonexistent1.txt"),
            Paths.get("nonexistent2.txt")
        );

        Set<String> commonWords = CommonWordsFinder.findCommonWords(documents, 2);
        assertTrue(commonWords.isEmpty(), "Should handle non-existent files gracefully");
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