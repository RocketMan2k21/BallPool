package com.example.parallel.lab4.src.main.java;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TextAnalysis {
    private static class WordLengthTask extends RecursiveTask<Map<Integer, Integer>> {
        private static final int THRESHOLD = 50_000;
        private final String[] words;
        private final int start;
        private final int end;
        private static final ConcurrentHashMap<Integer, Integer> sharedMap = new ConcurrentHashMap<>();

        public WordLengthTask(String[] words, int start, int end) {
            this.words = words;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Map<Integer, Integer> compute() {
            if (end - start <= THRESHOLD) {
                return computeDirectly();
            }
            
            int mid = start + (end - start) / 2;
            WordLengthTask left = new WordLengthTask(words, start, mid);
            WordLengthTask right = new WordLengthTask(words, mid, end);
            
            invokeAll(left, right);
            
            Map<Integer, Integer> result = new HashMap<>(left.join());
            right.join().forEach((k, v) -> result.merge(k, v, Integer::sum));
            return result;
        }

        private Map<Integer, Integer> computeDirectly() {
            Map<Integer, Integer> localMap = new HashMap<>();
            int batchSize = 1000;
            
            for (int i = start; i < end; i += batchSize) {
                int endBatch = Math.min(i + batchSize, end);
                for (int j = i; j < endBatch; j++) {
                    String word = words[j];
                    if (word != null && !word.isEmpty()) {
                        int length = word.length();
                        localMap.merge(length, 1, Integer::sum);
                    }
                }
            }
            return localMap;
        }
    }

    public static class TextStatistics {
        private final Map<Integer, Integer> wordLengths;
        private final int totalWords;
        private final double mean;
        private final double variance;
        private final double standardDeviation;
        private final int minLength;
        private final int maxLength;
        private final int mode;

        public TextStatistics(Map<Integer, Integer> wordLengths) {
            this.wordLengths = wordLengths;
            this.totalWords = calculateTotalWords();
            this.mean = calculateMean();
            this.variance = calculateVariance();
            this.standardDeviation = Math.sqrt(variance);
            this.minLength = calculateMinLength();
            this.maxLength = calculateMaxLength();
            this.mode = calculateMode();
        }

        private int calculateTotalWords() {
            return wordLengths.values().stream().mapToInt(Integer::intValue).sum();
        }

        private double calculateMean() {
            double sum = wordLengths.entrySet().stream()
                .mapToDouble(e -> e.getKey() * e.getValue())
                .sum();
            return sum / totalWords;
        }

        private double calculateVariance() {
            double sumSquaredDiff = wordLengths.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getKey() - mean, 2) * e.getValue())
                .sum();
            return sumSquaredDiff / totalWords;
        }

        private int calculateMinLength() {
            return wordLengths.keySet().stream().min(Integer::compareTo).orElse(0);
        }

        private int calculateMaxLength() {
            return wordLengths.keySet().stream().max(Integer::compareTo).orElse(0);
        }

        private int calculateMode() {
            return wordLengths.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        }

        @Override
        public String toString() {
            return String.format(
                "Статистика довжини слів:%n" +
                "Загальна кількість слів: %d%n" +
                "Середня довжина слова: %.2f%n" +
                "Дисперсія: %.2f%n" +
                "Стандартне відхилення: %.2f%n" +
                "Мінімальна довжина: %d%n" +
                "Максимальна довжина: %d%n" +
                "Мода: %d%n" +
                "Розподіл довжин слів:%n%s",
                totalWords, mean, variance, standardDeviation,
                minLength, maxLength, mode,
                formatDistribution()
            );
        }

        private String formatDistribution() {
            return wordLengths.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("  %d символів: %d слів (%.2f%%)",
                    e.getKey(), e.getValue(),
                    (double) e.getValue() / totalWords * 100))
                .collect(Collectors.joining("\n"));
        }
    }

    public static TextStatistics analyzeText(String[] words) {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        
        try {
            WordLengthTask task = new WordLengthTask(words, 0, words.length);
            Map<Integer, Integer> wordLengths = pool.invoke(task);
            return new TextStatistics(wordLengths);
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) {
        // Приклад тексту для аналізу
        String text = "Це приклад тексту для статистичного аналізу. " +
                     "Ми будемо аналізувати довжину слів у цьому тексті. " +
                     "Кожне слово має певну кількість символів. " +
                     "Статистичний аналіз допоможе нам зрозуміти розподіл довжин слів.";
        
        String[] words = text.split("\\s+");
        
        System.out.println("Аналіз тестового тексту:");
        System.out.println("------------------------");
        
        TextStatistics stats = analyzeText(words);
        System.out.println(stats);
        
        System.out.println("\nТестування продуктивності:");
        System.out.println("------------------------");
        testPerformance();
    }

    private static TextStatistics analyzeSequentially(String[] words) {
        Map<Integer, Integer> wordLengths = new HashMap<>();
        int batchSize = 1000;
        
        for (int i = 0; i < words.length; i += batchSize) {
            int endBatch = Math.min(i + batchSize, words.length);
            for (int j = i; j < endBatch; j++) {
                String word = words[j];
                if (word != null && !word.isEmpty()) {
                    int length = word.length();
                    wordLengths.merge(length, 1, Integer::sum);
                }
            }
        }
        return new TextStatistics(wordLengths);
    }

    private static void testPerformance() {
        int[] textSizes = {1_000, 10_000, 100_000, 1_000_000};
        int warmupSize = 100_000;
        
        // Прогріваємо JVM
        String[] warmupText = generateTestText(warmupSize);
        for (int i = 0; i < 3; i++) {
            analyzeSequentially(warmupText);
            analyzeText(warmupText);
        }
        
        for (int size : textSizes) {
            String[] words = generateTestText(size);
            
            // Виконуємо кілька ітерацій для кожного розміру
            int iterations = 5;
            double[] seqTimes = new double[iterations];
            double[] parTimes = new double[iterations];
            
            for (int i = 0; i < iterations; i++) {
                System.gc(); // Підказка для GC
                
                // Послідовне виконання
                long seqStart = System.nanoTime();
                TextStatistics seqStats = analyzeSequentially(words);
                seqTimes[i] = (System.nanoTime() - seqStart) / 1_000_000.0;
                
                System.gc(); // Підказка для GC
                
                // Паралельне виконання
                long parStart = System.nanoTime();
                TextStatistics parStats = analyzeText(words);
                parTimes[i] = (System.nanoTime() - parStart) / 1_000_000.0;
                
                // Перевірка коректності
                if (!validateResults(seqStats, parStats)) {
                    System.out.println("Помилка: результати не співпадають!");
                }
                
                try {
                    Thread.sleep(50); // Збільшена пауза між ітераціями
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Перериваємо тестування при отриманні сигналу переривання
                }
            }
            
            // Відкидаємо найгірший результат
            Arrays.sort(seqTimes);
            Arrays.sort(parTimes);
            
            // Обчислюємо середній час без найгіршого результату
            double avgSeqTime = Arrays.stream(seqTimes, 0, iterations - 1).average().orElse(0);
            double avgParTime = Arrays.stream(parTimes, 0, iterations - 1).average().orElse(0);
            
            System.out.printf("%nРозмір тексту: %d слів%n", size);
            System.out.printf("Послідовне виконання: %.2f мс%n", avgSeqTime);
            System.out.printf("Паралельне виконання: %.2f мс%n", avgParTime);
            System.out.printf("Прискорення: %.2fx%n", avgSeqTime / avgParTime);
            System.out.println("------------------------");
        }
    }

    private static boolean validateResults(TextStatistics seq, TextStatistics par) {
        return seq.totalWords == par.totalWords &&
               Math.abs(seq.mean - par.mean) < 0.001 &&
               seq.minLength == par.minLength &&
               seq.maxLength == par.maxLength;
    }

    private static String[] generateTestText(int size) {
        String[] words = new String[size];
        Random random = new Random();
        int[] commonLengths = {2, 3, 4, 5, 6, 7, 8, 9}; // Типові довжини слів
        
        for (int i = 0; i < size; i++) {
            int wordLength = commonLengths[random.nextInt(commonLengths.length)];
            words[i] = generateWord(wordLength, random);
        }
        return words;
    }
    
    private static String generateWord(int length, Random random) {
        char[] word = new char[length];
        for (int i = 0; i < length; i++) {
            word[i] = (char) ('а' + random.nextInt(32));
        }
        return new String(word);
    }
} 