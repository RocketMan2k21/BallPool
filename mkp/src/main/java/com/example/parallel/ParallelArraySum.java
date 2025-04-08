package com.example.parallel;

import java.util.concurrent.*;
import java.util.Arrays;

public class ParallelArraySum {
    private static final int ARRAY_SIZE = 1_000_000;
    private static final int NUM_TASKS = 100;
    private static final int NUM_THREADS = 8;

    public static void main(String[] args) {
        double[] array = new double[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            array[i] = Math.random();
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        int chunkSize = ARRAY_SIZE / NUM_TASKS;

        Future<Double>[] futures = new Future[NUM_TASKS];

        for (int i = 0; i < NUM_TASKS; i++) {
            final int startIndex = i * chunkSize;
            final int endIndex = (i == NUM_TASKS - 1) ? ARRAY_SIZE : (i + 1) * chunkSize;

            futures[i] = executor.submit(() -> {
                double sum = 0.0;
                for (int j = startIndex; j < endIndex; j++) {
                    sum += array[j];
                }
                return sum;
            });
        }

        double totalSum = 0.0;
        try {
            for (Future<Double> future : futures) {
                totalSum += future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        System.out.printf("Паралельна сума: %.6f%n", totalSum);

        double sequentialSum = Arrays.stream(array).sum();
        System.out.printf("Послідовна сума: %.6f%n", sequentialSum);
        System.out.printf("Різниця: %.10f%n", Math.abs(totalSum - sequentialSum));
    }
} 