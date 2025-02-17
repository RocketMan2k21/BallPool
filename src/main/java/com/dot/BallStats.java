package com.dot;

import java.awt.*;

class BallStats {
    private Color color;
    private Thread thread;
    private long distance;
    private long runTime;

    public BallStats(Color color, Thread thread) {
        this.color = color;
        this.thread = thread;
    }

    public void update(long distance, long runTime) {
        this.distance = distance;
        this.runTime = runTime;
    }

    @Override
    public String toString() {
        return String.format("%s Ball (Priority %d): Distance=%.2f, Time=%dms, Speed=%.2f",
                color == Color.RED ? "Red" : "Blue",
                thread.getPriority(),
                (double) distance,
                runTime,
                runTime > 0 ? (distance / (double)runTime) : 0);
    }
}
