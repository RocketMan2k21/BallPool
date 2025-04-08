package com.example.parallel;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

public class StateChangeExample {
    private enum State {
        R, W
    }

    private static final AtomicReference<State> currentState = new AtomicReference<>(State.R);
    private static volatile boolean isRunning = true;
    private static class ThreadA implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning) {
                    // Змінюємо стан
                    State newState = (currentState.get() == State.R) ? State.W : State.R;
                    currentState.set(newState);
                    System.out.printf("Потік A: змінив стан на %s%n", newState);
                    
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class ThreadB implements Runnable {
        @Override
        public void run() {
            ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
            
            try {
                while (isRunning) {
                    while (currentState.get() == State.W && isRunning) {
                        Thread.sleep(1);
                    }
                    
                    if (!isRunning) break;
                    
                    CountDownLatch countdownComplete = new CountDownLatch(1);
                    AtomicInteger countdown = new AtomicInteger(100);
                    
                    ScheduledFuture<?> countdownTask = timer.scheduleAtFixedRate(() -> {
                        int current = countdown.decrementAndGet();
                        if (current >= 0 && currentState.get() == State.R) {
                            System.out.printf("Потік B: відлік %d мс%n", current);
                        }
                        if (current <= 0 || currentState.get() == State.W) {
                            countdownComplete.countDown();
                        }
                    }, 0, 1, TimeUnit.MILLISECONDS);
                    
                    countdownComplete.await();
                    countdownTask.cancel(false);
                    if (currentState.get() == State.W) {
                        System.out.println("Потік B: призупинено (стан W)");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                timer.shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Thread threadA = new Thread(new ThreadA(), "A");
        Thread threadB = new Thread(new ThreadB(), "B");
        
        threadA.start();
        threadB.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
        
        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Програму завершено");
    }
} 