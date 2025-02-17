package com.dot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class BounceFrame extends JFrame {
    private BallCanvas canvas;
    private JLabel scoreLabel;
    private JTextArea statsArea;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce Program - Thread Priority Experiment");

        // Create stats area
        statsArea = new JTextArea(5, 40);
        statsArea.setEditable(false);

        this.canvas = new BallCanvas(statsArea);

        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);

        scoreLabel = new JLabel("Pocketed Balls: 0");
        controlPanel.add(scoreLabel, BorderLayout.NORTH);

        // Add buttons for different experiments
        JButton addRedBall = new JButton("Add Red Ball (High Priority)");
        JButton addBlueBall = new JButton("Add Blue Ball (Low Priority)");
        JButton addExperiment = new JButton("Run Priority Experiment");
        JButton buttonStop = new JButton("Stop");

        addRedBall.addActionListener(e -> addBall(Color.RED));
        addBlueBall.addActionListener(e -> addBall(Color.BLUE));
        addExperiment.addActionListener(e -> runPriorityExperiment());

        buttonStop.addActionListener(e -> System.exit(0));

        buttonPanel.add(addRedBall);
        buttonPanel.add(addBlueBall);
        buttonPanel.add(addExperiment);
        buttonPanel.add(buttonStop);

        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        controlPanel.add(new JScrollPane(statsArea), BorderLayout.SOUTH);

        content.add(controlPanel, BorderLayout.SOUTH);
    }

    private void addBall(Color color) {
        Ball b = new Ball(canvas, color);
        BallThread thread = new BallThread(b, color);
        canvas.add(b, thread);
        thread.start();
    }

    private void runPriorityExperiment() {
        // Add one red ball
        Ball redBall = new Ball(canvas, Color.RED);
        BallThread redThread = new BallThread(redBall, Color.RED);
        canvas.add(redBall, redThread);

        // Add multiple blue balls
        for (int i = 0; i < 50; i++) {
            Ball blueBall = new Ball(canvas, Color.BLUE);
            BallThread blueThread = new BallThread(blueBall, Color.BLUE);
            canvas.add(blueBall, blueThread);
        }

        // Start all threads
        canvas.getThreads().forEach(Thread::start);
    }
}