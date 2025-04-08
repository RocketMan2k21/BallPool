package com.example.parallel.lab4.src.main.java.part2;

import com.example.parallel.lab4.src.main.java.MatrixUtils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ForkJoinMatrixMultiplication {
    private static final int THRESHOLD = 64; // Threshold for sequential computation

    public static MatrixUtils.Result multiply(double[][] a, double[][] b, int threadCount) {
        int n = a.length;
        double[][] result = new double[n][n];

        long startTime = System.nanoTime();

        ForkJoinPool pool = new ForkJoinPool(threadCount);
        FoxMultiplicationTask task = new FoxMultiplicationTask(a, b, result, 0, n, 0, n, 0, n);
        pool.invoke(task);
        pool.shutdown();

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        return new MatrixUtils.Result(result, executionTime);
    }

    private static class FoxMultiplicationTask extends RecursiveAction {
        private final double[][] A;
        private final double[][] B;
        private final double[][] C;
        private final int rowStartA;
        private final int rowEndA;
        private final int colStartB;
        private final int colEndB;
        private final int kStart;
        private final int kEnd;

        public FoxMultiplicationTask(double[][] A, double[][] B, double[][] C,
                                   int rowStartA, int rowEndA,
                                   int colStartB, int colEndB,
                                   int kStart, int kEnd) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.rowStartA = rowStartA;
            this.rowEndA = rowEndA;
            this.colStartB = colStartB;
            this.colEndB = colEndB;
            this.kStart = kStart;
            this.kEnd = kEnd;
        }

        @Override
        protected void compute() {
            int rowSizeA = rowEndA - rowStartA;
            int colSizeB = colEndB - colStartB;
            int kSize = kEnd - kStart;

            // If the submatrix is small enough, compute sequentially
            if (rowSizeA <= THRESHOLD && colSizeB <= THRESHOLD && kSize <= THRESHOLD) {
                multiplySequentially();
                return;
            }

            // Otherwise, split the task into smaller subtasks
            int rowMidA = rowStartA + rowSizeA / 2;
            int colMidB = colStartB + colSizeB / 2;
            int kMid = kStart + kSize / 2;

            invokeAll(
                // Top-left quadrant
                new FoxMultiplicationTask(A, B, C, 
                    rowStartA, rowMidA, colStartB, colMidB, kStart, kMid),
                // Top-right quadrant
                new FoxMultiplicationTask(A, B, C,
                    rowStartA, rowMidA, colMidB, colEndB, kStart, kMid),
                // Bottom-left quadrant
                new FoxMultiplicationTask(A, B, C,
                    rowMidA, rowEndA, colStartB, colMidB, kStart, kMid),
                // Bottom-right quadrant
                new FoxMultiplicationTask(A, B, C,
                    rowMidA, rowEndA, colMidB, colEndB, kStart, kMid),
                // Second half of k range
                new FoxMultiplicationTask(A, B, C,
                    rowStartA, rowMidA, colStartB, colMidB, kMid, kEnd),
                new FoxMultiplicationTask(A, B, C,
                    rowStartA, rowMidA, colMidB, colEndB, kMid, kEnd),
                new FoxMultiplicationTask(A, B, C,
                    rowMidA, rowEndA, colStartB, colMidB, kMid, kEnd),
                new FoxMultiplicationTask(A, B, C,
                    rowMidA, rowEndA, colMidB, colEndB, kMid, kEnd)
            );
        }

        private void multiplySequentially() {
            for (int i = rowStartA; i < rowEndA; i++) {
                for (int j = colStartB; j < colEndB; j++) {
                    double sum = 0;
                    for (int k = kStart; k < kEnd; k++) {
                        sum += A[i][k] * B[k][j];
                    }
                    synchronized (C) {
                        C[i][j] += sum;
                    }
                }
            }
        }
    }
} 