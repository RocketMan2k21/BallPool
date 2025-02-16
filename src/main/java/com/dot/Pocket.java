package com.dot;

import java.awt.*;
import java.awt.geom.Ellipse2D;

class Pocket {
    private int x;
    private int y;
    private static final int RADIUS = 30;

    public Pocket(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean catchesBall(Ball ball) {
        int ballCenterX = ball.getX() + ball.getSize() / 2;
        int ballCenterY = ball.getY() + ball.getSize() / 2;
        double distance = Math.sqrt(Math.pow(ballCenterX - x, 2) + Math.pow(ballCenterY - y, 2));
        return distance < RADIUS;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Double(x - RADIUS/2, y - RADIUS/2, RADIUS, RADIUS));
    }
}
