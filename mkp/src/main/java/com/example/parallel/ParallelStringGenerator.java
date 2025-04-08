package com.example.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class ParallelStringGenerator {
    private static final int NUM_THREADS = 4;
    private static final int ITERATIONS = 1000;
    private static final int STRING_LENGTH = 10;

    public static void main(String[] args) {
        // Створюємо потокобезпечну колекцію
        List<String> sharedCollection = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        Future<?>[] futures = new Future[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            futures[i] = executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < ITERATIONS; j++) {
                    String randomString = generateRandomString(random);
                    sharedCollection.add(randomString);
                    System.out.printf("Потік %d: Додано рядок %d: %s%n", 
                        threadId, j, randomString);
                }
            });
        }
        
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
        
        System.out.printf("%nЗагальна кількість згенерованих рядків: %d%n",
            sharedCollection.size());
        System.out.printf("Очікувана кількість: %d%n", 
            NUM_THREADS * ITERATIONS);
    }
    
    private static String generateRandomString(Random random) {
        StringBuilder sb = new StringBuilder(STRING_LENGTH);
        for (int i = 0; i < STRING_LENGTH; i++) {
            char randomChar = (char) (random.nextInt(26) + 'a');
            sb.append(randomChar);
        }
        return sb.toString();
    }
} 