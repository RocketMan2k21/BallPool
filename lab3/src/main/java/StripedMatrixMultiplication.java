import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StripedMatrixMultiplication {
    public static MatrixUtils.Result multiply(double[][] a, double[][] b, int threadCount) {
        int n = a.length;
        double[][] result = new double[n][n];

        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int stripeHeight = n / threadCount;

        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
            final int startRow = threadIndex * stripeHeight;
            final int endRow = (threadIndex == threadCount - 1) ? n : startRow + stripeHeight;

            executor.submit(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < n; j++) {
                        double sum = 0;
                        for (int k = 0; k < n; k++) {
                            sum += a[i][k] * b[k][j];
                        }
                        result[i][j] = sum;
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000; // Час у мілісекундах

        return new MatrixUtils.Result(result, executionTime);
    }
}