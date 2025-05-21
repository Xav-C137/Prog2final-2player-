/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;

public final class TwoPlayerGamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 800;
    static final int UNIT_SIZE = 40;
    static final int DELAY = 95;
    static final int MAX_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    final int[] x1 = new int[MAX_UNITS];
    final int[] y1 = new int[MAX_UNITS];
    final int[] x2 = new int[MAX_UNITS];
    final int[] y2 = new int[MAX_UNITS];

    int bodyParts1 = 2;
    int bodyParts2 = 2;
    int applesEaten1 = 0;
    int applesEaten2 = 0;
    int appleX, appleY;

    char direction1 = 'L';
    char direction2 = 'R';

    boolean running = false;
    boolean directionChanged1 = false;
    boolean directionChanged2 = false;

    Timer gameTimer;
    Timer countdownTimer;
    JButton restartButton;
    JButton exitButton;
    String winnerText = "";
    Image[] backgrounds;
    long lastAppleTime1 = System.currentTimeMillis();
    long lastAppleTime2 = System.currentTimeMillis();
    int countdown = 3;
    int backgroundIndex = 0;

    Random random = new Random();
    Image appleImage = new ImageIcon("apple.png").getImage();
    Image snakeHead1 = new ImageIcon("head.png").getImage();
    Image snakeBody1 = new ImageIcon("body.png").getImage();
    Image snakeHead2 = new ImageIcon("head2.png").getImage();
    Image snakeBody2 = new ImageIcon("body2.png").getImage();

    public TwoPlayerGamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        backgrounds = new Image[]{
            new ImageIcon("bg1.png").getImage(),
            new ImageIcon("bg2.png").getImage(),
            new ImageIcon("bg3.png").getImage()
        };
        initPositions();
        startCountdown();
    }

    private void initPositions() {
        x1[0] = SCREEN_WIDTH - UNIT_SIZE * 2;
        y1[0] = SCREEN_HEIGHT - UNIT_SIZE * 2;
        x2[0] = UNIT_SIZE;
        y2[0] = UNIT_SIZE;
        for (int i = 1; i < MAX_UNITS; i++) {
            x1[i] = y1[i] = -UNIT_SIZE;
            x2[i] = y2[i] = -UNIT_SIZE;
        }
    }

    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown <= 0) {
                countdownTimer.stop();
                startGame();
            }
            repaint();
        });
        countdownTimer.start();
    }

    private void startGame() {
        newApple();
        running = true;
        gameTimer = new Timer(DELAY, this);
        gameTimer.start();
    }

    public void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, SCREEN_WIDTH - 1, SCREEN_HEIGHT - 1);
        if (backgrounds != null && backgrounds.length > 0) {
            g.drawImage(backgrounds[backgroundIndex], 0, 0, 800, 800, this);
        }
        if (countdown > 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 72));
            String text = countdown + "";
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString(text, (SCREEN_WIDTH - metrics.stringWidth(text)) / 2, SCREEN_HEIGHT / 2);
            return;
        }

        if (running) {
            g.drawImage(appleImage, appleX, appleY, UNIT_SIZE, UNIT_SIZE, this);
            Graphics2D g2d = (Graphics2D) g;

            for (int i = 0; i < bodyParts1; i++) {
                drawSnakeSegment(g2d, x1, y1, i, snakeHead1, snakeBody1, direction1);
            }

            for (int i = 0; i < bodyParts2; i++) {
                drawSnakeSegment(g2d, x2, y2, i, snakeHead2, snakeBody2, direction2);
            }

            int timeLeft1 = 18 - (int)((System.currentTimeMillis() - lastAppleTime1) / 1000);
            int timeLeft2 = 18 - (int)((System.currentTimeMillis() - lastAppleTime2) / 1000);
            timeLeft1 = Math.max(timeLeft1, 0);
            timeLeft2 = Math.max(timeLeft2, 0);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(Color.YELLOW);
            g.drawString("P1 Timer: " + timeLeft1, 10, 50);
            g.drawString("P2 Timer: " + timeLeft2, SCREEN_WIDTH - 150, 50);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("P1: " + applesEaten1, 10, 20);
            g.drawString("P2: " + applesEaten2, SCREEN_WIDTH - 100, 20);
        }

        if (!running && countdown <= 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 64));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString(winnerText, (SCREEN_WIDTH - metrics.stringWidth(winnerText)) / 2, SCREEN_HEIGHT / 2 - 50);
        }
    }

    private void drawSnakeSegment(Graphics2D g2d, int[] x, int[] y, int i, Image head, Image body, char dir) {
        double angle = 0;
        if (i == 0) {
            angle = switch (dir) {
                case 'U' -> Math.toRadians(270);
                case 'D' -> Math.toRadians(90);
                case 'L' -> Math.toRadians(180);
                case 'R' -> 0;
                default -> 0;
            };
            drawRotatedImage(g2d, head, x[i], y[i], angle);
        } else {
            int dx = x[i - 1] - x[i];
            int dy = y[i - 1] - y[i];
            if (dx > 0) angle = 0;
            else if (dx < 0) angle = Math.PI;
            else if (dy > 0) angle = Math.PI / 2;
            else if (dy < 0) angle = 3 * Math.PI / 2;
            drawRotatedImage(g2d, body, x[i], y[i], angle);
        }
    }

    private void drawRotatedImage(Graphics2D g2d, Image img, int x, int y, double angle) {
        int cx = x + UNIT_SIZE / 2;
        int cy = y + UNIT_SIZE / 2;
        g2d.translate(cx, cy);
        g2d.rotate(angle);
        g2d.drawImage(img, -UNIT_SIZE / 2, -UNIT_SIZE / 2, UNIT_SIZE, UNIT_SIZE, this);
        g2d.rotate(-angle);
        g2d.translate(-cx, -cy);
    }

    public void move(int[] x, int[] y, int bodyParts, char direction) {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        boolean appleEaten = false;

        if (x1[0] == appleX && y1[0] == appleY) {
            bodyParts1++;
            applesEaten1++;
            lastAppleTime1 = System.currentTimeMillis();
            appleEaten = true;
        } else if (x2[0] == appleX && y2[0] == appleY) {
            bodyParts2++;
            applesEaten2++;
            lastAppleTime2 = System.currentTimeMillis();
            appleEaten = true;
        }

        if (appleEaten) {
            playSound("eat.wav");
            newApple();
            int totalEaten = applesEaten1 + applesEaten2;
            if (totalEaten % 10 == 0) {
                backgroundIndex = (backgroundIndex + 1) % backgrounds.length;
                int currentDelay = gameTimer.getDelay();
                gameTimer.setDelay(Math.max(30, currentDelay - 10));
            }
        }
    }

    public void checkCollisions() {
        if (countdown > 0) return;

        boolean p1Hit = false, p2Hit = false;

        if (x1[0] < 0 || x1[0] >= SCREEN_WIDTH || y1[0] < 0 || y1[0] >= SCREEN_HEIGHT) p1Hit = true;
        if (x2[0] < 0 || x2[0] >= SCREEN_WIDTH || y2[0] < 0 || y2[0] >= SCREEN_HEIGHT) p2Hit = true;

        for (int i = 1; i < bodyParts1; i++)
            if (x1[0] == x1[i] && y1[0] == y1[i]) p1Hit = true;

        for (int i = 1; i < bodyParts2; i++)
            if (x2[0] == x2[i] && y2[0] == y2[i]) p2Hit = true;

        if (p1Hit && p2Hit) {
            winnerText = "Draw!";
        } else if (p1Hit) {
            winnerText = "Player 2 Wins!";
        } else if (p2Hit) {
            winnerText = "Player 1 Wins!";
        }

        if (p1Hit || p2Hit) {
            playSound("death.wav");
            endGame();
        }
    }

    private void endGame() {
        running = false;
        gameTimer.stop();
        showGameOverButtons();
        repaint();
    }

    private void showGameOverButtons() {
        restartButton = new JButton("Restart");
        exitButton = new JButton("Exit");
        restartButton.setBounds(SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 + 50, 200, 40);
        exitButton.setBounds(SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 + 100, 200, 40);
        restartButton.setFocusable(false);
        exitButton.setFocusable(false);
        this.setLayout(null);
        this.add(restartButton);
        this.add(exitButton);
        restartButton.addActionListener(e -> restartGame());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void restartGame() {
        this.remove(restartButton);
        this.remove(exitButton);
        this.setLayout(new BorderLayout());
        applesEaten1 = 0;
        applesEaten2 = 0;
        bodyParts1 = 2;
        bodyParts2 = 2;
        direction1 = 'L';
        direction2 = 'R';
        backgroundIndex = 0;
        countdown = 3;
        directionChanged1 = false;
        directionChanged2 = false;
        long now = System.currentTimeMillis();
        lastAppleTime1 = now;
        lastAppleTime2 = now;
        initPositions();
        startCountdown();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();

        if (now - lastAppleTime1 >= 18000) {
            winnerText = "Player 2 Wins! Player 1 timed out!";
            playSound("death.wav");
            endGame();
            return;
        }

        if (now - lastAppleTime2 >= 18000) {
            winnerText = "Player 1 Wins! Player 2 timed out!";
            playSound("death.wav");
            endGame();
            return;
        }

        if (countdown <= 0 && running) {
            move(x1, y1, bodyParts1, direction1);
            move(x2, y2, bodyParts2, direction2);
            checkApple();
            checkCollisions();
            directionChanged1 = false;
            directionChanged2 = false;
        }

        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!running || countdown > 0) return;
            if (directionChanged1 && directionChanged2) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { if (direction1 != 'R') direction1 = 'L'; directionChanged1 = true; }
                case KeyEvent.VK_RIGHT -> { if (direction1 != 'L') direction1 = 'R'; directionChanged1 = true; }
                case KeyEvent.VK_UP -> { if (direction1 != 'D') direction1 = 'U'; directionChanged1 = true; }
                case KeyEvent.VK_DOWN -> { if (direction1 != 'U') direction1 = 'D'; directionChanged1 = true; }
                case KeyEvent.VK_A -> { if (direction2 != 'R') direction2 = 'L'; directionChanged2 = true; }
                case KeyEvent.VK_D -> { if (direction2 != 'L') direction2 = 'R'; directionChanged2 = true; }
                case KeyEvent.VK_W -> { if (direction2 != 'D') direction2 = 'U'; directionChanged2 = true; }
                case KeyEvent.VK_S -> { if (direction2 != 'U') direction2 = 'D'; directionChanged2 = true; }
            }
        }
    }
}
