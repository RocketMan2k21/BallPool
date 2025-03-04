package org.example.first;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AsynchBankAtomic {
    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10000;

    public static void main(String[] args) {
        Bank b = new Bank(NACCOUNTS, INITIAL_BALANCE);
        for (int i = 0; i < NACCOUNTS; i++) {
            TransferThread t = new TransferThread(b, i, INITIAL_BALANCE);
            t.setPriority(Thread.NORM_PRIORITY + i % 2);
            t.start();
        }
    }

    static class Bank {
        private static final int NTEST = 10000;
        private final AtomicInteger[] accounts;
        private final AtomicLong ntransacts = new AtomicLong(0);

        public Bank(int n, int initialBalance) {
            accounts = new AtomicInteger[n];
            for (int i = 0; i < accounts.length; i++)
                accounts[i] = new AtomicInteger(initialBalance);
        }

        public void transfer(int from, int to, int amount) {
            synchronized (this) {
                while (accounts[from].get() < amount) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                accounts[from].addAndGet(-amount);
                accounts[to].addAndGet(amount);

                long currentTransacts = ntransacts.incrementAndGet();
                if (currentTransacts % NTEST == 0)
                    test();

                notifyAll();
            }
        }

        public synchronized void test() {
            int sum = 0;
            for (AtomicInteger account : accounts)
                sum += account.get();
            System.out.println("Transactions:" + ntransacts.get() + " Sum: " + sum);
        }

        public int size() {
            return accounts.length;
        }
    }

    static class TransferThread extends Thread {
        private Bank bank;
        private int fromAccount;
        private int maxAmount;
        private static final int REPS = 1000;

        public TransferThread(Bank b, int from, int max) {
            bank = b;
            fromAccount = from;
            maxAmount = max;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    for (int i = 0; i < REPS; i++) {
                        int toAccount = (int) (bank.size() * Math.random());
                        int amount = (int) (maxAmount * Math.random() / REPS);
                        bank.transfer(fromAccount, toAccount, amount);
                        Thread.sleep(1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}