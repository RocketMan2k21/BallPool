public class MatrixMultiplicationExperiments {
    public static void main(String[] args) {
        // Розміри матриць для експериментів
        int[] sizes = {512, 1024, 2048};
        // Кількість потоків
        int[] threadCounts = {1, 2, 4, 8, 16};

        System.out.println("Експерименти з множення матриць:");

        for (int size : sizes) {
            System.out.printf("\nРозмір матриці: %d x %d\n", size, size);

            // Генерація матриць
            double[][] a = MatrixUtils.generateMatrix(size);
            double[][] b = MatrixUtils.generateMatrix(size);

            // Послідовне множення для перевірки
            double[][] sequential = MatrixUtils.multiplySequential(a, b);

            System.out.println("\nЕксперименти зі стрічкового алгоритму:");
            for (int threads : threadCounts) {
                MatrixUtils.Result stripedResult =
                        StripedMatrixMultiplication.multiply(a, b, threads);

                boolean valid = MatrixUtils.validateResult(
                        sequential, stripedResult.getData());

                System.out.printf(
                        "Потоки: %2d, Час: %5d мс, Коректність: %b\n",
                        threads,
                        stripedResult.getExecutionTime(),
                        valid
                );
            }

            System.out.println("\nЕксперименти з алгоритму Фокса:");
            for (int threads : threadCounts) {
                MatrixUtils.Result foxResult =
                        FoxMatrixMultiplication.multiply(a, b, threads);

                boolean valid = MatrixUtils.validateResult(
                        sequential, foxResult.getData());

                System.out.printf(
                        "Потоки: %2d, Час: %5d мс, Коректність: %b\n",
                        threads,
                        foxResult.getExecutionTime(),
                        valid
                );
            }
        }
    }
}