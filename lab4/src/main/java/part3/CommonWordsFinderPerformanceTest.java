package com.example.parallel.lab4.src.main.java.part3;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

class CommonWordsFinderPerformanceTest {
    private static final String TEST_DIR = "performance_test_documents";
    private Path testDirPath;

    @BeforeEach
    void setUp() throws IOException {
        testDirPath = Paths.get(TEST_DIR);
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
    void testPerformanceWithDifferentSizes() throws IOException {
        int[] fileSizes = {1000, 10000, 100000}; // Words per file
        int[] threadCounts = {1, 2, 4, 8, 16};
        int numFiles = 5;

        System.out.println("\nPerformance Test Results:");
        System.out.println("=========================");

        for (int size : fileSizes) {
            System.out.printf("\nFile size: %d words per file, %d files%n", size, numFiles);
            System.out.println("----------------------------------------");
            
            // Generate test files
            List<String> baseWords = generateWords(size);
            for (int i = 0; i < numFiles; i++) {
                createTestFile("file" + i + ".txt", 
                    shuffleAndJoinWords(baseWords, 0.8)); // 80% common words
            }

            List<Path> documents = getTestDocuments();

            // Test with different thread counts
            for (int threadCount : threadCounts) {
                long totalTime = 0;
                int runs = 3; // Number of runs for averaging

                for (int run = 0; run < runs; run++) {
                    long startTime = System.nanoTime();
                    Set<String> result = CommonWordsFinder.findCommonWords(documents, threadCount);
                    long endTime = System.nanoTime();
                    totalTime += (endTime - startTime) / 1_000_000; // Convert to milliseconds
                }

                double avgTime = totalTime / (double) runs;
                System.out.printf("Threads: %2d, Average Time: %8.2f ms%n", 
                    threadCount, avgTime);
            }
        }
    }

    @Test
    void testScalability() throws IOException {
        int[] numFiles = {2, 4, 8, 16};
        int wordsPerFile = 10000;
        int threadCount = Runtime.getRuntime().availableProcessors();

        System.out.println("\nScalability Test Results:");
        System.out.println("========================");
        System.out.printf("Words per file: %d, Thread count: %d%n", wordsPerFile, threadCount);
        System.out.println("----------------------------------------");

        for (int fileCount : numFiles) {
            // Generate test files
            List<String> baseWords = generateWords(wordsPerFile);
            for (int i = 0; i < fileCount; i++) {
                createTestFile("file" + i + ".txt", 
                    shuffleAndJoinWords(baseWords, 0.7)); // 70% common words
            }

            List<Path> documents = getTestDocuments();
            long totalTime = 0;
            int runs = 3;

            for (int run = 0; run < runs; run++) {
                long startTime = System.nanoTime();
                Set<String> result = CommonWordsFinder.findCommonWords(documents, threadCount);
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime) / 1_000_000;
            }

            double avgTime = totalTime / (double) runs;
            System.out.printf("Files: %2d, Average Time: %8.2f ms%n", 
                fileCount, avgTime);
        }
    }

    private List<String> generateWords(int count) {
        List<String> words = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            StringBuilder word = new StringBuilder();
            int length = random.nextInt(5) + 5; // Words of length 5-10
            for (int j = 0; j < length; j++) {
                word.append((char) (random.nextInt(26) + 'a'));
            }
            words.add(word.toString());
        }
        return words;
    }

    private String shuffleAndJoinWords(List<String> baseWords, double commonRatio) {
        Random random = new Random();
        int commonCount = (int) (baseWords.size() * commonRatio);
        
        // Take common words
        List<String> words = new ArrayList<>(baseWords.subList(0, commonCount));
        
        // Add unique words
        for (int i = commonCount; i < baseWords.size(); i++) {
            StringBuilder word = new StringBuilder();
            int length = random.nextInt(5) + 5;
            for (int j = 0; j < length; j++) {
                word.append((char) (random.nextInt(26) + 'a'));
            }
            words.add(word.toString());
        }
        
        Collections.shuffle(words);
        return String.join(" ", words);
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