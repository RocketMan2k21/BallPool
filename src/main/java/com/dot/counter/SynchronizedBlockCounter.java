package com.dot.counter;

class SynchronizedBlockCounter {
    private int count = 0;
    private final Object lock = new Object();

    public void increment() {
        synchronized(lock) {
            count++;
        }
    }

    public void decrement() {
        synchronized(lock) {
            count--;
        }
    }

    public int getCount() {
        synchronized(lock) {
            return count;
        }
    }
}
