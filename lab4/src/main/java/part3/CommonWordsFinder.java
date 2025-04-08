package com.example.parallel.lab4.src.main.java.part3;

import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

public class CommonWordsFinder {
    private static final int THRESHOLD = 1000; // Threshold for sequential processing

    public static Set<String> findCommonWords(List<Path> documents, int threadCount) {
        ForkJoinPool pool = new ForkJoinPool(threadCount);
        CommonWordsTask task = new CommonWordsTask(documents, 0, documents.size());
        Set<String> result = pool.invoke(task);
        pool.shutdown();
        return result;
    }

    private static class CommonWordsTask extends RecursiveTask<Set<String>> {
        private final List<Path> documents;
        private final int start;
        private final int end;

        public CommonWordsTask(List<Path> documents, int start, int end) {
            this.documents = documents;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Set<String> compute() {
            int size = end - start;
            
            // If the number of documents is small enough, process sequentially
            if (size <= THRESHOLD) {
                return computeSequentially();
            }

            // Split the task into two subtasks
            int mid = start + size / 2;
            CommonWordsTask leftTask = new CommonWordsTask(documents, start, mid);
            CommonWordsTask rightTask = new CommonWordsTask(documents, mid, end);

            // Fork the left task and compute the right task
            leftTask.fork();
            Set<String> rightResult = rightTask.compute();
            Set<String> leftResult = leftTask.join();

            // Find intersection of results
            rightResult.retainAll(leftResult);
            return rightResult;
        }

        private Set<String> computeSequentially() {
            Set<String> commonWords = null;
            
            for (int i = start; i < end; i++) {
                try {
                    Set<String> words = getWordsFromFile(documents.get(i));
                    if (commonWords == null) {
                        commonWords = new HashSet<>(words);
                    } else {
                        commonWords.retainAll(words);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + documents.get(i));
                    e.printStackTrace();
                }
            }
            
            return commonWords != null ? commonWords : Collections.emptySet();
        }

        private Set<String> getWordsFromFile(Path file) throws IOException {
            String content = new String(Files.readAllBytes(file));
            return Arrays.stream(content.toLowerCase()
                    .replaceAll("[^a-zA-Z\\s]", "")
                    .split("\\s+"))
                    .filter(word -> word.length() > 0)
                    .collect(Collectors.toSet());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java CommonWordsFinder <directory_path> <thread_count>");
            return;
        }

        String directoryPath = args[0];
        int threadCount = Integer.parseInt(args[1]);

        try {
            List<Path> documents = Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            if (documents.isEmpty()) {
                System.out.println("No text files found in the specified directory");
                return;
            }

            System.out.println("Processing " + documents.size() + " documents...");
            long startTime = System.nanoTime();

            Set<String> commonWords = findCommonWords(documents, threadCount);

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

            System.out.println("\nCommon words found: " + commonWords.size());
            System.out.println("Execution time: " + executionTime + " ms");
            System.out.println("\nCommon words:");
            commonWords.stream()
                    .sorted()
                    .forEach(word -> System.out.println("- " + word));

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }
} 