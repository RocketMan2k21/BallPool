package com.dot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class BounceFrame extends JFrame {
    private BallCanvas canvas;
    private JLabel scoreLabel;
    public static final int WIDTH = 450;
    public static final int HEIGHT = 350;

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce Program");
        this.canvas = new BallCanvas();

        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);

        // Add score label
        scoreLabel = new JLabel("Pocketed Balls: 0");
        controlPanel.add(scoreLabel, BorderLayout.NORTH);

        JButton buttonStart = new JButton("Start");
        JButton buttonStop = new JButton("Stop");

        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Ball b = new Ball(canvas);
                canvas.add(b);
                BallThread thread = new BallThread(b);
                thread.start();

                // Start score update timer
                updateScore();
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(buttonStart);
        buttonPanel.add(buttonStop);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        content.add(controlPanel, BorderLayout.SOUTH);
    }

    private void updateScore() {
        Timer timer = new Timer(100, e -> {
            scoreLabel.setText("Pocketed Balls: " + canvas.getPocketedBallsCount());
        });
        timer.start();
    }
}
