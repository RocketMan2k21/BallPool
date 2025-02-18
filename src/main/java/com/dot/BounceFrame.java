package com.dot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

class BounceFrame extends JFrame {
    private BallCanvas canvas;
    private JLabel scoreLabel;
    private JTextArea statsArea;
    private JButton joinDemoButton;

    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce Program - Thread Priority Experiment");

        // Create stats area
        statsArea = new JTextArea(5, 40);
        statsArea.setEditable(false);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        scoreLabel = new JLabel("Pocketed Balls: 0");
        controlPanel.add(scoreLabel, BorderLayout.NORTH);

        this.canvas = new BallCanvas(statsArea, scoreLabel);

        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);

        joinDemoButton = new JButton("Run Join Demo");
        joinDemoButton.addActionListener(e -> runJoinDemo());

        buttonPanel.add(joinDemoButton);

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
        Ball b = new Ball(canvas, color,
                canvas.getWidth() / 2,
                canvas.getHeight() / 2);
        BallThread thread = new BallThread(b, color);
        canvas.add(b, thread);
        thread.start();
    }

    private void runPriorityExperiment() {
        // Add one red ball

        canvas.getThreads().clear();
        Ball redBall = new Ball(canvas, Color.RED,
                canvas.getWidth() / 4,
                canvas.getHeight() / 2);
        BallThread redThread = new BallThread(redBall, Color.RED);
        canvas.add(redBall, redThread);

        // Add multiple blue balls
        for (int i = 0; i < 50; i++) {
            Ball blueBall = new Ball(canvas, Color.BLUE,
                    canvas.getWidth() / 4,
                    canvas.getHeight() / 2);
            BallThread blueThread = new BallThread(blueBall, Color.BLUE);
            canvas.add(blueBall, blueThread);
        }

        // Start all threads
        canvas.getThreads().forEach(Thread::start);
    }

    private void runJoinDemo() {
        // Disable the button during demo
        joinDemoButton.setEnabled(false);
        canvas.getThreads().clear();

        // Create a sequence of balls with different colors
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        BallThread previousThread = null;
        ArrayList<Ball> balls = new ArrayList<>();

        // Starting positions for each ball
        int[] startX = {50, 150, 250, 350};
        int[] moves = {200, 150, 100, 50}; // Different movement counts for each ball

        // Create the sequence of balls and threads
        for (int i = 0; i < colors.length; i++) {
            Ball ball = new Ball(canvas, colors[i], startX[i], 50);
            balls.add(ball);
            BallThread thread = new BallThreadLimited(ball, colors[i], previousThread, moves[i]);
            canvas.add(ball, thread);
            previousThread = thread;
        }

        // Start monitoring thread
        new Thread(() -> {
            try {
                // Start all threads in reverse order
                for (int i = balls.size() - 1; i >= 0; i--) {
                    canvas.getThreads().get(i).start();
                }

                // Wait for all balls to complete
                boolean allCompleted;
                do {
                    allCompleted = true;
                    for (Ball ball : balls) {
                        if (!ball.isCompleted()) {
                            allCompleted = false;
                            break;
                        }
                    }
                    Thread.sleep(100);
                } while (!allCompleted);

                // Re-enable the button
                SwingUtilities.invokeLater(() -> {
                    joinDemoButton.setEnabled(true);
                    statsArea.append("\nJoin Demo completed!\n");
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        statsArea.setText("Join Demo started!\n" +
                "Yellow ball waits for Green\n" +
                "Green ball waits for Blue\n" +
                "Blue ball waits for Red\n" +
                "Red ball starts immediately\n");
    }
}