package org.example.first;

public class AsynchBankSynchronized {
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
        private final int[] accounts;
        private long ntransacts = 0;

        public Bank(int n, int initialBalance) {
            accounts = new int[n];
            for (int i = 0; i < accounts.length; i++)
                accounts[i] = initialBalance;
            ntransacts = 0;
        }

        // Синхронізований метод для безпечного виконання транзакцій
        public synchronized void transfer(int from, int to, int amount) {
            try {
                while (accounts[from] < amount) {
                    // Очікування, якщо недостатньо коштів
                    wait();
                }

                accounts[from] -= amount;
                accounts[to] += amount;
                ntransacts++;

                if (ntransacts % NTEST == 0)
                    test();

                // Сповіщення інших потоків про зміну стану
                notifyAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public synchronized void test() {
            int sum = 0;
            for (int account : accounts)
                sum += account;
            System.out.println("Transactions:" + ntransacts + " Sum: " + sum);
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
                        Thread.sleep(1); // Невелика затримка для зменшення конфліктів
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}