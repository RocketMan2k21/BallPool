package com.dot;

import java.awt.*;

public class BallThreadLimited extends BallThread{
    private int movesCount;
    private BallThread threadToWaitFor;

    public BallThreadLimited(Ball ball, Color color, BallThread waitForThread, int movesCount) {
        super(ball, color);
        this.movesCount = movesCount;
        this.threadToWaitFor = waitForThread;
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
                stats.update(b.getDistanceTraveled(), b.getRunningTime());
                Thread.sleep(5);
            }
            b.setCompleted(true);

        } catch (InterruptedException ex) {
            // Handle interruption
        }
    }

}
