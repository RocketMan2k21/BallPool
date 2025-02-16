package com.dot;

class BallThread extends Thread {
    private Ball b;

    public BallThread(Ball ball) {
        b = ball;
    }

    @Override
    public void run() {
        try {
            while (b.isActive()) {
                b.move();
                Thread.sleep(5);
            }
        } catch (InterruptedException ex) {
            // Handle interruption
        }
    }
}
