package org.example.second;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.Random;

public class ProducerConsumerApp {
    private static final int BUFFER_SIZE = 100;
    private static final int TOTAL_ELEMENTS = 1000;

    public static void main(String[] args) {
        ArrayBlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(BUFFER_SIZE);

        Producer producer = new Producer(buffer, TOTAL_ELEMENTS);
        Consumer consumer = new Consumer(buffer, TOTAL_ELEMENTS);

        new Thread(producer).start();
        new Thread(consumer).start();
    }

    static class Producer implements Runnable {
        private final ArrayBlockingQueue<Integer> buffer;
        private final int totalElements;
        private final Random random = new Random();

        public Producer(ArrayBlockingQueue<Integer> buffer, int totalElements) {
            this.buffer = buffer;
            this.totalElements = totalElements;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < totalElements; i++) {
                    int value = random.nextInt(1000);
                    buffer.put(value);
                    System.out.println("Produced: " + value);
                    Thread.sleep(10); // Імітація часу виробництва
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final ArrayBlockingQueue<Integer> buffer;
        private final int totalElements;
        private int processedElements = 0;

        public Consumer(ArrayBlockingQueue<Integer> buffer, int totalElements) {
            this.buffer = buffer;
            this.totalElements = totalElements;
        }

        @Override
        public void run() {
            try {
                while (processedElements < totalElements) {
                    Integer value = buffer.take();
                    System.out.println("Consumed: " + value);
                    processedElements++;
                    Thread.sleep(20); // Імітація часу споживання
                }
                System.out.println("All elements processed: " + processedElements);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}