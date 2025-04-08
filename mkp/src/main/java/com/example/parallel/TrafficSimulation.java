package com.example.parallel;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrafficSimulation {
    private static final int NUM_CARS = 100;
    private static final int MAX_CARS_PASSED = 1000;
    private static final AtomicInteger carsPassed = new AtomicInteger(0);
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);
    private static final CountDownLatch completionLatch = new CountDownLatch(1);

    private enum TrafficLight {
        GREEN(70), YELLOW1(10), RED(40), YELLOW2(10);

        private final int delay;

        TrafficLight(int delay) {
            this.delay = delay;
        }

        public int getDelay() {
            return delay;
        }

        public TrafficLight next() {
            switch (this) {
                case GREEN: return YELLOW1;
                case YELLOW1: return RED;
                case RED: return YELLOW2;
                case YELLOW2: return GREEN;
                default: return GREEN;
            }
        }
    }

    private static class TrafficLightThread implements Runnable {
        private TrafficLight currentState = TrafficLight.GREEN;
        private final Semaphore greenLightSemaphore = new Semaphore(1, true);
        private final AtomicBoolean isGreen = new AtomicBoolean(false);

        public TrafficLightThread() {
            try {
                greenLightSemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    if (carsPassed.get() >= MAX_CARS_PASSED) {
                        isRunning.set(false);
                        completionLatch.countDown();
                        break;
                    }

                    switch (currentState) {
                        case GREEN:
                            isGreen.set(true);
                            greenLightSemaphore.release();
                            System.out.println("Світлофор: ЗЕЛЕНЕ");
                            break;
                        case YELLOW1:
                        case YELLOW2:
                            isGreen.set(false);
                            greenLightSemaphore.tryAcquire();
                            System.out.println("Світлофор: ЖОВТЕ");
                            break;
                        case RED:
                            isGreen.set(false);
                            greenLightSemaphore.tryAcquire();
                            System.out.println("Світлофор: ЧЕРВОНЕ");
                            break;
                    }

                    Thread.sleep(currentState.getDelay());
                    currentState = currentState.next();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            greenLightSemaphore.tryAcquire();
            isGreen.set(false);
        }

        public Semaphore getGreenLightSemaphore() {
            return greenLightSemaphore;
        }

        public boolean isGreen() {
            return isGreen.get();
        }
    }

    private static class CarThread implements Runnable {
        private final int carId;
        private final TrafficLightThread trafficLight;

        public CarThread(int carId, TrafficLightThread trafficLight) {
            this.carId = carId;
            this.trafficLight = trafficLight;
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    if (carsPassed.get() >= MAX_CARS_PASSED) {
                        break;
                    }

                    // Try to acquire green light permission
                    if (trafficLight.isGreen() && trafficLight.getGreenLightSemaphore().tryAcquire()) {
                        try {
                            // Double check if we can still proceed
                            if (!trafficLight.isGreen() || carsPassed.get() >= MAX_CARS_PASSED) {
                                trafficLight.getGreenLightSemaphore().release();
                                continue;
                            }

                            System.out.printf("Автомобіль %d проїжджає%n", carId);
                            Thread.sleep(2); // Час проїзду

                            int passed = carsPassed.incrementAndGet();
                            if (passed >= MAX_CARS_PASSED) {
                                isRunning.set(false);
                                completionLatch.countDown();
                                break;
                            }
                        } finally {
                            trafficLight.getGreenLightSemaphore().release();
                        }
                    }

                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CARS + 1);

        TrafficLightThread trafficLight = new TrafficLightThread();
        executor.submit(trafficLight);

        for (int i = 0; i < NUM_CARS; i++) {
            executor.submit(new CarThread(i, trafficLight));
        }

        try {
            completionLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        isRunning.set(false);
        executor.shutdownNow();

        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.printf("%nЗагальна кількість проїхавших автомобілів: %d%n",
            carsPassed.get());
    }
}