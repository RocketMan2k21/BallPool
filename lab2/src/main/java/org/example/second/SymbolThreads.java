package org.example.second;

import java.util.concurrent.Semaphore;

public class SymbolThreads {
    private static final int ROWS = 90;
    private static Semaphore semaphore = new Semaphore(1);
    private static volatile int currentThread = 0;

    public static void main(String[] args) {
        // Послідовне виведення |\/|\/|\/
        SymbolThread vertical = new SymbolThread('|', 0);
        SymbolThread backSlash = new SymbolThread('\\', 1);
        SymbolThread forwardSlash = new SymbolThread('/', 2);

        vertical.start();
        backSlash.start();
        forwardSlash.start();

        try {
            vertical.join();
            backSlash.join();
            forwardSlash.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class SymbolThread extends Thread {
        private char symbol;
        private int threadId;

        public SymbolThread(char symbol, int threadId) {
            this.symbol = symbol;
            this.threadId = threadId;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < ROWS; i++) {
                    semaphore.acquire();
                    if (currentThread == threadId) {
                        System.out.print(symbol);
                        currentThread = (currentThread + 1) % 3;
                        if ((i + 1) % 30 == 0) System.out.println();
                    }
                    semaphore.release();
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}