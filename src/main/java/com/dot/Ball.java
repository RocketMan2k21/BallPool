package com.dot;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

class Ball {
    private Component canvas;
    private static final int XSIZE = 20;
    private static final int YSIZE = 20;
    private int x = 0;
    private int y = 0;
    private int dx = 2;
    private int dy = 2;
    private Color color;
    private boolean active = true;
    private long startTime;
    private long distanceTraveled = 0;

    public Ball(Component c, Color color) {
        this.canvas = c;
        this.color = color;
        this.startTime = System.currentTimeMillis();

        if(Math.random()<0.5){
            x = new Random().nextInt(this.canvas.getWidth());
            y = 0;
        }else{
            x = 0;
            y = new Random().nextInt(this.canvas.getHeight());
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return XSIZE; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }

    public void draw(Graphics2D g2) {
        if (active) {
            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(x, y, XSIZE, YSIZE));
        }
    }

    public long getDistanceTraveled() {
        return distanceTraveled;
    }

    public long getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void move() {
        if (!active) return;

        int oldX = x;
        int oldY = y;

        x += dx;
        y += dy;

        // Calculate distance traveled
        distanceTraveled += Math.sqrt(Math.pow(x - oldX, 2) + Math.pow(y - oldY, 2));

        // Boundary checks remain the same...
        if (x < 0) {
            x = 0;
            dx = -dx;
        }
        if (x + XSIZE >= this.canvas.getWidth()) {
            x = this.canvas.getWidth() - XSIZE;
            dx = -dx;
        }
        if (y < 0) {
            y = 0;
            dy = -dy;
        }
        if (y + YSIZE >= this.canvas.getHeight()) {
            y = this.canvas.getHeight() - YSIZE;
            dy = -dy;
        }
        this.canvas.repaint();
    }
}
