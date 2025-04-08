package com.example.parallel;

import java.util.concurrent.*;

public class BoundedBufferExample {
    private static final int BUFFER_SIZE = 5;
    private static final int NUM_PRODUCERS = 2;
    private static final int NUM_CONSUMERS = 1;
    private static final int NUM_ITEMS = 20;

    private static class BoundedBuffer {
        private final Object[] buffer;
        private int count = 0;
        private int putIndex = 0;
        private int takeIndex = 0;
        private final Semaphore items;
        private final Semaphore spaces;
        private final Semaphore mutex;

        public BoundedBuffer(int size) {
            buffer = new Object[size];
            items = new Semaphore(0);
            spaces = new Semaphore(size);
            mutex = new Semaphore(1);
        }

        public void put(Object item) throws InterruptedException {
            spaces.acquire();
            mutex.acquire();
            try {
                buffer[putIndex] = item;
                putIndex = (putIndex + 1) % buffer.length;
                count++;
                System.out.printf("Потік %s додав об'єкт. Розмір буфера: %d%n", 
                    Thread.currentThread().getName(), count);
            } finally {
                mutex.release();
                items.release();
            }
        }

        public Object take() throws InterruptedException {
            items.acquire();
            mutex.acquire();
            try {
                Object item = buffer[takeIndex];
                buffer[takeIndex] = null;
                takeIndex = (takeIndex + 1) % buffer.length;
                count--;
                System.out.printf("Потік %s вилучив об'єкт. Розмір буфера: %d%n", 
                    Thread.currentThread().getName(), count);
                return item;
            } finally {
                mutex.release();
                spaces.release();
            }
        }
    }

    private static class Producer implements Runnable {
        private final BoundedBuffer buffer;
        private final int id;

        public Producer(BoundedBuffer buffer, int id) {
            this.buffer = buffer;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < NUM_ITEMS; i++) {
                    Object item = new Object();
                    buffer.put(item);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Клас для потоку-споживача (C)
    private static class Consumer implements Runnable {
        private final BoundedBuffer buffer;

        public Consumer(BoundedBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < NUM_ITEMS * NUM_PRODUCERS; i++) {
                    Object item = buffer.take();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        BoundedBuffer buffer = new BoundedBuffer(BUFFER_SIZE);
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_PRODUCERS + NUM_CONSUMERS);
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            executor.submit(new Producer(buffer, i));
        }
        executor.submit(new Consumer(buffer));
        executor.shutdown();
        
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 