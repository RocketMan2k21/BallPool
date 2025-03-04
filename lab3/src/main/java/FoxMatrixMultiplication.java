import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FoxMatrixMultiplication {
    public static MatrixUtils.Result multiply(double[][] a, double[][] b, int threadCount) {
        int n = a.length;
        double[][] result = new double[n][n];

        long startTime = System.nanoTime();

        int blockSize = n / threadCount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int threadRow = 0; threadRow < threadCount; threadRow++) {
            for (int threadCol = 0; threadCol < threadCount; threadCol++) {
                final int finalThreadRow = threadRow;
                final int finalThreadCol = threadCol;

                executor.submit(() -> {
                    for (int k = 0; k < threadCount; k++) {
                        int rowStart = finalThreadRow * blockSize;
                        int colStart = finalThreadCol * blockSize;
                        int kRowStart = k * blockSize;
                        int kColStart = k * blockSize;

                        multiplyBlock(a, b, result,
                                rowStart, colStart,
                                kRowStart, kColStart,
                                blockSize);
                    }
                });
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;

        return new MatrixUtils.Result(result, executionTime);
    }

    private static void multiplyBlock(
            double[][] a, double[][] b, double[][] result,
            int rowStart, int colStart,
            int kRowStart, int kColStart,
            int blockSize
    ) {
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                double sum = 0;
                for (int k = 0; k < blockSize; k++) {
                    sum += a[rowStart + i][kRowStart + k] *
                            b[kColStart + k][colStart + j];
                }
                result[rowStart + i][colStart + j] += sum;
            }
        }
    }
}