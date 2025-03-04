package com.dot.counter;

class CounterTest {
    private static final int ITERATIONS = 100000;
    private static final int THREAD_PAIRS = 10;

    public static void testCounter(String counterType, Runnable incrementTask, Runnable decrementTask) {
        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[THREAD_PAIRS * 2];

        // Create increment and decrement threads
        for (int i = 0; i < THREAD_PAIRS; i++) {
            threads[i * 2] = new Thread(incrementTask);
            threads[i * 2 + 1] = new Thread(decrementTask);
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();

        System.out.println(counterType + " completed in " + (endTime - startTime) + "ms");
    }

    public static void main(String[] args) {
        // Test unsynchronized counter
        UnsynchronizedCounter unsyncCounter = new UnsynchronizedCounter();
        System.out.println("\nTesting Unsynchronized Counter:");
        testCounter("Unsynchronized Counter",
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        unsyncCounter.increment();
                    }
                },
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        unsyncCounter.decrement();
                    }
                }
        );
        System.out.println("Final count: " + unsyncCounter.getCount());

        // Test synchronized method counter
        SynchronizedMethodCounter syncMethodCounter = new SynchronizedMethodCounter();
        System.out.println("\nTesting Synchronized Method Counter:");
        testCounter("Synchronized Method Counter",
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        syncMethodCounter.increment();
                    }
                },
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        syncMethodCounter.decrement();
                    }
                }
        );
        System.out.println("Final count: " + syncMethodCounter.getCount());

        // Test synchronized block counter
        SynchronizedBlockCounter syncBlockCounter = new SynchronizedBlockCounter();
        System.out.println("\nTesting Synchronized Block Counter:");
        testCounter("Synchronized Block Counter",
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        syncBlockCounter.increment();
                    }
                },
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        syncBlockCounter.decrement();
                    }
                }
        );
        System.out.println("Final count: " + syncBlockCounter.getCount());

        // Test lock counter
        LockCounter lockCounter = new LockCounter();
        System.out.println("\nTesting Lock Counter:");
        testCounter("Lock Counter",
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        lockCounter.increment();
                    }
                },
                () -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        lockCounter.decrement();
                    }
                }
        );
        System.out.println("Final count: " + lockCounter.getCount());
    }
}
