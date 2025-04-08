package com.example.parallel.lab4.src.main.java.part4;

import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

public class ITDocumentSearcher {
    private static final int THRESHOLD = 10; // Number of files to process sequentially
    private static final Set<String> IT_KEYWORDS = new HashSet<>(Arrays.asList(
            "programming", "software", "development", "java", "python", "algorithm",
            "database", "sql", "network", "security", "web", "cloud", "api",
            "framework", "testing", "agile", "devops", "git", "docker", "kubernetes",
            "machine learning", "artificial intelligence", "data science", "big data",
            "blockchain", "cybersecurity", "encryption", "server", "client", "http",
            "rest", "json", "xml", "html", "css", "javascript", "code", "compiler",
            "debugging", "frontend", "backend", "fullstack", "architecture", "design patterns",
            "microservices", "continuous integration", "deployment", "scaling", "optimization"
    ));

    public static class SearchResult {
        private final Path filePath;
        private final Set<String> matchedKeywords;
        private final double relevanceScore;

        public SearchResult(Path filePath, Set<String> matchedKeywords, double relevanceScore) {
            this.filePath = filePath;
            this.matchedKeywords = matchedKeywords;
            this.relevanceScore = relevanceScore;
        }

        public Path getFilePath() {
            return filePath;
        }

        public Set<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public double getRelevanceScore() {
            return relevanceScore;
        }

        @Override
        public String toString() {
            return String.format("File: %s%nRelevance: %.2f%%%nKeywords: %s%n",
                    filePath.getFileName(),
                    relevanceScore * 100,
                    String.join(", ", matchedKeywords));
        }
    }

    public static List<SearchResult> searchDocuments(List<Path> documents, int threadCount) {
        ForkJoinPool pool = new ForkJoinPool(threadCount);
        DocumentSearchTask task = new DocumentSearchTask(documents, 0, documents.size());
        List<SearchResult> results = pool.invoke(task);
        pool.shutdown();
        
        // Sort results by relevance score in descending order
        results.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));
        return results;
    }

    private static class DocumentSearchTask extends RecursiveTask<List<SearchResult>> {
        private final List<Path> documents;
        private final int start;
        private final int end;

        public DocumentSearchTask(List<Path> documents, int start, int end) {
            this.documents = documents;
            this.start = start;
            this.end = end;
        }

        @Override
        protected List<SearchResult> compute() {
            int size = end - start;
            
            if (size <= THRESHOLD) {
                return computeSequentially();
            }

            int mid = start + size / 2;
            DocumentSearchTask leftTask = new DocumentSearchTask(documents, start, mid);
            DocumentSearchTask rightTask = new DocumentSearchTask(documents, mid, end);

            leftTask.fork();
            List<SearchResult> rightResults = rightTask.compute();
            List<SearchResult> leftResults = leftTask.join();

            // Combine results
            List<SearchResult> combinedResults = new ArrayList<>(leftResults);
            combinedResults.addAll(rightResults);
            return combinedResults;
        }

        private List<SearchResult> computeSequentially() {
            List<SearchResult> results = new ArrayList<>();
            
            for (int i = start; i < end; i++) {
                try {
                    Path file = documents.get(i);
                    SearchResult result = analyzeDocument(file);
                    if (result.getRelevanceScore() > 0) {
                        results.add(result);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + documents.get(i));
                    e.printStackTrace();
                }
            }
            
            return results;
        }

        private SearchResult analyzeDocument(Path file) throws IOException {
            String content = new String(Files.readAllBytes(file)).toLowerCase();
            Set<String> matchedKeywords = new HashSet<>();

            // Find all matching keywords
            for (String keyword : IT_KEYWORDS) {
                if (content.contains(keyword.toLowerCase())) {
                    matchedKeywords.add(keyword);
                }
            }

            // Calculate relevance score based on keyword matches
            double relevanceScore = matchedKeywords.size() / (double) IT_KEYWORDS.size();

            return new SearchResult(file, matchedKeywords, relevanceScore);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ITDocumentSearcher <directory_path> <thread_count>");
            return;
        }

        String directoryPath = args[0];
        int threadCount = Integer.parseInt(args[1]);

        try {
            List<Path> documents = Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.toString().toLowerCase();
                        return fileName.endsWith(".txt") || 
                               fileName.endsWith(".doc") || 
                               fileName.endsWith(".pdf") ||
                               fileName.endsWith(".md");
                    })
                    .collect(Collectors.toList());

            if (documents.isEmpty()) {
                System.out.println("No documents found in the specified directory");
                return;
            }

            System.out.println("Processing " + documents.size() + " documents...");
            long startTime = System.nanoTime();

            List<SearchResult> results = searchDocuments(documents, threadCount);

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

            System.out.println("\nSearch completed in " + executionTime + " ms");
            System.out.println("Found " + results.size() + " relevant documents\n");
            
            // Print top 10 most relevant documents
            System.out.println("Top 10 most relevant documents:");
            System.out.println("===============================");
            results.stream()
                    .limit(10)
                    .forEach(result -> System.out.println(result + "---"));

        } catch (IOException e) {
            System.err.println("Error processing directory: " + e.getMessage());
        }
    }
} 