package com.example.parallel.lab4.src.main.java.part2;

import com.example.parallel.lab4.src.main.java.MatrixUtils;

public class MatrixMultiplicationComparison {
    public static void main(String[] args) {
        // Розміри матриць для експериментів
        int[] sizes = {512, 1024, 2048};
        // Кількість потоків
        int[] threadCounts = {1, 2, 4, 8, 16};

        System.out.println("Порівняння алгоритмів множення матриць:");
        System.out.println("----------------------------------------");

        for (int size : sizes) {
            System.out.printf("\nРозмір матриці: %d x %d\n", size, size);
            System.out.println("----------------------------------------");

            // Генерація матриць
            double[][] a = MatrixUtils.generateMatrix(size);
            double[][] b = MatrixUtils.generateMatrix(size);

            // Послідовне множення для перевірки
            System.out.println("\nПослідовне множення:");
            long startTime = System.nanoTime();
            double[][] sequential = MatrixUtils.multiplySequential(a, b);
            long endTime = System.nanoTime();
            long sequentialTime = (endTime - startTime) / 1_000_000;
            System.out.printf("Час виконання: %d мс\n", sequentialTime);

            System.out.println("\nАлгоритм Фокса (ExecutorService):");
            for (int threads : threadCounts) {
                MatrixUtils.Result foxResult = FoxMatrixMultiplication.multiply(a, b, threads);
                boolean valid = MatrixUtils.validateResult(sequential, foxResult.getData());
                double speedup = (double) sequentialTime / foxResult.getExecutionTime();

                System.out.printf(
                    "Потоки: %2d, Час: %5d мс, Прискорення: %.2fx, Коректність: %b\n",
                    threads,
                    foxResult.getExecutionTime(),
                    speedup,
                    valid
                );
            }

            System.out.println("\nАлгоритм Фокса (ForkJoin):");
            for (int threads : threadCounts) {
                MatrixUtils.Result forkJoinResult = ForkJoinMatrixMultiplication.multiply(a, b, threads);
                boolean valid = MatrixUtils.validateResult(sequential, forkJoinResult.getData());
                double speedup = (double) sequentialTime / forkJoinResult.getExecutionTime();

                System.out.printf(
                    "Потоки: %2d, Час: %5d мс, Прискорення: %.2fx, Коректність: %b\n",
                    threads,
                    forkJoinResult.getExecutionTime(),
                    speedup,
                    valid
                );
            }
        }
    }
} 