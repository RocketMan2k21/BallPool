package com.dot;

import java.awt.*;

class BallThread extends Thread {
    protected Ball b;
    private BallStats stats;

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

    public BallStats getStats() {
        return stats;
    }

    @Override
    public void run() {
        try {
            while (b.isActive()) {
                b.move();
                stats.update(b.getDistanceTraveled(), b.getRunningTime());
                Thread.sleep(5);
            }
            b.setCompleted(true);
        } catch (InterruptedException ex) {
            // Handle interruption
        }
    }
}
