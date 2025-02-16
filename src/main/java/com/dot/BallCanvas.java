package com.dot;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

class BallCanvas extends JPanel {
    private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<Pocket> pockets = new ArrayList<>();
    private AtomicInteger pocketedBalls = new AtomicInteger(0);

    public BallCanvas() {
        // Add pockets in corners and middle of sides
        addPockets();
    }

    private void addPockets() {
        // Corners
        pockets.add(new Pocket(0, 0));
        pockets.add(new Pocket(getWidth(), 0));
        pockets.add(new Pocket(0, getHeight()));
        pockets.add(new Pocket(getWidth(), getHeight()));
        // Middle of sides
        pockets.add(new Pocket(getWidth()/2, 0));
        pockets.add(new Pocket(getWidth()/2, getHeight()));
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        pockets.clear();
        addPockets();
    }

    public void add(Ball b) {
        balls.add(b);
    }

    public int getPocketedBallsCount() {
        return pocketedBalls.get();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw pockets
        for (Pocket pocket : pockets) {
            pocket.draw(g2);
        }

        // Draw active balls and check for pocketed balls
        for (Ball b : balls) {
            if (b.isActive()) {
                for (Pocket pocket : pockets) {
                    if (pocket.catchesBall(b)) {
                        b.deactivate();
                        pocketedBalls.incrementAndGet();
                        break;
                    }
                }
                b.draw(g2);
            }
        }
    }
}
