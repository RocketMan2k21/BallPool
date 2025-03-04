import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatrixUtils {
    // Клас для зберігання результату множення
    public static class Result {
        private double[][] data;
        private long executionTime;

        public Result(double[][] data, long executionTime) {
            this.data = data;
            this.executionTime = executionTime;
        }

        public double[][] getData() {
            return data;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        // Метод для виведення розміру матриці
        public int getSize() {
            return data.length;
        }
    }

    // Генерація матриці з випадковими значеннями
    public static double[][] generateMatrix(int size) {
        double[][] matrix = new double[size][size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextDouble() * 10;
            }
        }
        return matrix;
    }

    // Друк матриці
    public static void printMatrix(double[][] matrix) {
        if (matrix.length > 20) {
            System.out.println("Матриця занадто велика для повного виведення");
            return;
        }
        for (double[] row : matrix) {
            for (double val : row) {
                System.out.printf("%8.2f ", val);
            }
            System.out.println();
        }
    }

    // Послідовне множення матриць для перевірки
    public static double[][] multiplySequential(double[][] a, double[][] b) {
        int n = a.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    // Перевірка правильності паралельного множення
    public static boolean validateResult(double[][] sequential, double[][] parallel) {
        if (sequential.length != parallel.length) return false;

        for (int i = 0; i < sequential.length; i++) {
            for (int j = 0; j < sequential.length; j++) {
                if (Math.abs(sequential[i][j] - parallel[i][j]) > 1e-6) {
                    return false;
                }
            }
        }
        return true;
    }
}