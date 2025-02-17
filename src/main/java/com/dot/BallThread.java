package com.dot;

import java.awt.*;

class BallThread extends Thread {
    private Ball b;
    private BallStats stats;
    private BallThread threadToWaitFor;
    private int movesCount;

    public BallThread(Ball ball, Color color) {
        b = ball;
        // Set thread priority based on ball color
        if (color == Color.RED) {
            setPriority(Thread.MAX_PRIORITY); // 10
        } else {
            setPriority(Thread.MIN_PRIORITY); // 1
        }
        stats = new BallStats(color, this);
    }

    public BallThread(Ball ball, BallThread waitFor, int moves) {
        b = ball;
        threadToWaitFor = waitFor;
        movesCount = moves;
    }

    public BallStats getStats() {
        return stats;
    }

    @Override
    public void run() {
        try {
            // Wait for the other thread if specified
            if (threadToWaitFor != null) {
                threadToWaitFor.join();
            }

            int moves = 0;
            while (b.isActive() && moves < movesCount) {
                b.move();
                moves++;
                Thread.sleep(5);
            }
            b.setCompleted(true);

        } catch (InterruptedException ex) {
            // Handle interruption
        }
    }
}
