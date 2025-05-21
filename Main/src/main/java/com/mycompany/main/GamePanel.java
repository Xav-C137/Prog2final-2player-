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

public final class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 800;
    static final int UNIT_SIZE = 40;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 95;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 2;
    int applesEaten;
    int appleX;
    int appleY;
    int highScore;
    char direction = 'R';
    boolean running = false;
    boolean directionChanged = false;
    Timer timer;
    Random random;
    JButton restartButton;
    JButton exitButton;
    Image[] backgrounds;
    int backgroundIndex = 0;

    private final String player;

    Image snakeBody;
    Image snakeHead;

    public GamePanel(String player) {
        this.player = player;
        this.highScore = Main.userScores.getOrDefault(player, 0);

        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        backgrounds = new Image[]{
                new ImageIcon("bg1.png").getImage(),
                new ImageIcon("bg2.png").getImage(),
                new ImageIcon("bg3.png").getImage()
        };

        snakeBody = new ImageIcon("body.png").getImage();
        snakeHead = new ImageIcon("head.png").getImage();

        startGame();
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

    public void startGame() {
        x[0] = SCREEN_WIDTH / 2;
        y[0] = SCREEN_HEIGHT / 2;
        for (int i = 1; i < x.length; i++) {
        x[i] = -UNIT_SIZE;
        y[i] = -UNIT_SIZE;
    }
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }   

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, SCREEN_WIDTH - 1, SCREEN_HEIGHT - 1);
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, SCREEN_WIDTH - 1, SCREEN_HEIGHT - 1);  // 1-pixel wide outline
        if (backgrounds != null && backgrounds.length > 0) {
            g.drawImage(backgrounds[backgroundIndex], 0, 0, 800, 800, this);
        }
        if (running) {
            g.drawImage(new ImageIcon("apple.png").getImage(), appleX, appleY, UNIT_SIZE, UNIT_SIZE, this);
            Graphics2D g2d = (Graphics2D) g;

            for (int i = 0; i < bodyParts; i++) {
                double angle = 0;
                if (i == 0) {
                    angle = switch (direction) {
                        case 'U' -> Math.toRadians(270);
                        case 'D' -> Math.toRadians(90);
                        case 'L' -> Math.toRadians(180);
                        case 'R' -> 0;
                        default -> 0;
                    };
                    drawRotatedImage(g2d, snakeHead, x[i], y[i], angle);
                } else {
                    int dx = x[i - 1] - x[i];
                    int dy = y[i - 1] - y[i];

                    if (dx > 0) angle = 0;
                    else if (dx < 0) angle = Math.PI;
                    else if (dy > 0) angle = Math.PI / 2;
                    else if (dy < 0) angle = 3 * Math.PI / 2;

                    drawRotatedImage(g2d, snakeBody, x[i], y[i], angle);
                }
            }
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, 
                   (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2,
                    g.getFont().getSize());          
        } else {               
                gameOver(g);
        } 
    }

    public void drawRotatedImage(Graphics2D g2d, Image img, int x, int y, double angle) {
        int cx = x + UNIT_SIZE / 2;
        int cy = y + UNIT_SIZE / 2;
        g2d.translate(cx, cy);
        g2d.rotate(angle);
        g2d.drawImage(img, -UNIT_SIZE / 2, -UNIT_SIZE / 2, UNIT_SIZE, UNIT_SIZE, this);
        g2d.rotate(-angle);
        g2d.translate(-cx, -cy);
    }

    public void newApple() {
        boolean onSnake;
        do  {
            onSnake = false;    
            appleX = random.nextInt((SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            for (int i = 0; i < bodyParts; i++) {
                if (x[i] == appleX && y[i] == appleY) {
                onSnake = true;
                break;
                }
            }
        } while (onSnake);
    }
    public void move() {
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
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            playSound("eat.wav");
            newApple();

            if (applesEaten % 10 == 0) {
                backgroundIndex = (backgroundIndex + 1) % backgrounds.length;
                int newDelay = Math.max(30, timer.getDelay() - 10);
                timer.setDelay(newDelay);
            }
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                playSound("death.wav");
                running = false;
            }
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            playSound("death.wav");
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }
    
    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
    
    // Display current score
        g.drawString("Score: " + applesEaten,
           (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2,
           SCREEN_HEIGHT / 3);

    // Display high score
        g.drawString("High Score: " + highScore,
           (SCREEN_WIDTH - metrics1.stringWidth("High Score: " + highScore)) / 2,
           SCREEN_HEIGHT / 3 + 40);
        
    //Highscore
        Main.updateScore(player, applesEaten);
        highScore = Math.max(highScore, applesEaten); 
        
        // Game over text
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("GAME OVER",
           (SCREEN_WIDTH - metrics2.stringWidth("GAME  OVER")) / 2,
           SCREEN_HEIGHT / 2);
 
    // Again and Exit bttns
        if (restartButton == null) {
            this.setLayout(null);

            restartButton = new JButton("Again");
            restartButton.setBounds((SCREEN_WIDTH / 2) - 110, SCREEN_HEIGHT / 2 + 90, 100, 50);
            restartButton.addActionListener(e -> restartGame());
            this.add(restartButton);

            exitButton = new JButton("Exit");
            exitButton.setBounds((SCREEN_WIDTH / 2) + 10, SCREEN_HEIGHT / 2 + 90, 100, 50);
            exitButton.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                  window.dispose(); 
                }
                Main.main(null);
            });
            this.add(exitButton);
            this.repaint();
        }
    }
   
    public void restartGame() {
        bodyParts = 2;
        applesEaten = 0;
        direction = 'R';
        running = true;       
        x[0] = SCREEN_WIDTH / 2;
        y[0] = SCREEN_HEIGHT / 2;        
        for (int i = 1  ; i < x.length; i++) {
            x[i] = -UNIT_SIZE;
            y[i] = -UNIT_SIZE;
        }
        backgroundIndex = 0;
        timer.setDelay(DELAY);
        newApple();
        timer.start();
        if (restartButton != null) {
            this.remove(restartButton);
            restartButton = null;
        }
        if (exitButton != null) {
            this.remove(exitButton);
            exitButton = null;
        }
        this.requestFocusInWindow();
        repaint();      
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
            directionChanged = false;
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (directionChanged) return;
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') direction = 'L';
                        directionChanged = true;
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') direction = 'R';
                        directionChanged = true;
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') direction = 'U';
                        directionChanged = true;
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') direction = 'D';
                      directionChanged = true;
                }
            }
        }
    }
}


/*
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;

public final class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 800;
    static final int UNIT_SIZE = 40;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 95;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 2;
    int applesEaten;
    int appleX;
    int appleY;
    int highScore;
    char direction = 'R';
    boolean running = false;
    boolean directionChanged = false;
    Timer timer;
    Random random;
    JButton restartButton;
    JButton exitButton;
    Image[] backgrounds;
    int backgroundIndex = 0;

    private final String player; //player name

    Image snakeBody;
    Image snakeHead;

    public GamePanel(String player) {
        this.player = player;
        this.highScore = Main.userScores.getOrDefault(player, 0);

        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        backgrounds = new Image[]{
                new ImageIcon("bg1.png").getImage(),
                new ImageIcon("bg2.png").getImage(),
                new ImageIcon("bg3.png").getImage()
        };

        snakeBody = new ImageIcon("body.png").getImage();
        snakeHead = new ImageIcon("head.png").getImage();

        startGame();
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

    public void startGame() {
        x[0] = SCREEN_WIDTH / 2;
        y[0] = SCREEN_HEIGHT / 2;
        for (int i = 1; i < x.length; i++) {
        x[i] = -UNIT_SIZE;
        y[i] = -UNIT_SIZE;
    }
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }   

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, SCREEN_WIDTH - 1, SCREEN_HEIGHT - 1);
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, SCREEN_WIDTH - 1, SCREEN_HEIGHT - 1);  // 1-pixel wide outline
        if (backgrounds != null && backgrounds.length > 0) {
            g.drawImage(backgrounds[backgroundIndex], 0, 0, 800, 800, this);
        }
        if (running) {
            g.drawImage(new ImageIcon("apple.png").getImage(), appleX, appleY, UNIT_SIZE, UNIT_SIZE, this);
            Graphics2D g2d = (Graphics2D) g;

            for (int i = 0; i < bodyParts; i++) {
                double angle = 0;
                if (i == 0) {
                    angle = switch (direction) {
                        case 'U' -> Math.toRadians(270);
                        case 'D' -> Math.toRadians(90);
                        case 'L' -> Math.toRadians(180);
                        case 'R' -> 0;
                        default -> 0;
                    };
                    drawRotatedImage(g2d, snakeHead, x[i], y[i], angle);
                } else {
                    int dx = x[i - 1] - x[i];
                    int dy = y[i - 1] - y[i];

                    if (dx > 0) angle = 0;
                    else if (dx < 0) angle = Math.PI;
                    else if (dy > 0) angle = Math.PI / 2;
                    else if (dy < 0) angle = 3 * Math.PI / 2;

                    drawRotatedImage(g2d, snakeBody, x[i], y[i], angle);
                }
            }
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, 
                   (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2,
                    g.getFont().getSize());          
        } else {               
                gameOver(g);
        } 
    }

    public void drawRotatedImage(Graphics2D g2d, Image img, int x, int y, double angle) {
        int cx = x + UNIT_SIZE / 2;
        int cy = y + UNIT_SIZE / 2;
        g2d.translate(cx, cy);
        g2d.rotate(angle);
        g2d.drawImage(img, -UNIT_SIZE / 2, -UNIT_SIZE / 2, UNIT_SIZE, UNIT_SIZE, this);
        g2d.rotate(-angle);
        g2d.translate(-cx, -cy);
    }

    public void newApple() {
        boolean onSnake;
        do  {
            onSnake = false;    
            appleX = random.nextInt((SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            for (int i = 0; i < bodyParts; i++) {
                if (x[i] == appleX && y[i] == appleY) {
                onSnake = true;
                break;
                }
            }
        } while (onSnake);
    }
    public void move() {
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
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            playSound("eat.wav");
            newApple();

            if (applesEaten % 10 == 0) {
                backgroundIndex = (backgroundIndex + 1) % backgrounds.length;
                int newDelay = Math.max(30, timer.getDelay() - 10);
                timer.setDelay(newDelay);
            }
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                playSound("death.wav");
                running = false;
            }
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            playSound("death.wav");
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }
    
    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
    
    // Display current score
        g.drawString("Score: " + applesEaten,
           (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2,
           SCREEN_HEIGHT / 3);

    // Display high score
        g.drawString("High Score: " + highScore,
           (SCREEN_WIDTH - metrics1.stringWidth("High Score: " + highScore)) / 2,
           SCREEN_HEIGHT / 3 + 40);
        
    //Highscore
        Main.updateScore(player, applesEaten);
        highScore = Math.max(highScore, applesEaten); 
        
        // Game over text
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("GAME OVER",
           (SCREEN_WIDTH - metrics2.stringWidth("GAME  OVER")) / 2,
           SCREEN_HEIGHT / 2);
 
    // Again and Exit bttns
        if (restartButton == null) {
            this.setLayout(null);

            restartButton = new JButton("Again");
            restartButton.setBounds((SCREEN_WIDTH / 2) - 110, SCREEN_HEIGHT / 2 + 90, 100, 50);
            restartButton.addActionListener(e -> restartGame());
            this.add(restartButton);

            exitButton = new JButton("Exit");
            exitButton.setBounds((SCREEN_WIDTH / 2) + 10, SCREEN_HEIGHT / 2 + 90, 100, 50);
            exitButton.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                  window.dispose(); 
                }
                Main.main(null);
            });
            this.add(exitButton);
            this.repaint();
        }
    }
   
    public void restartGame() {
        bodyParts = 2;
        applesEaten = 0;
        direction = 'R';
        running = true;       
        x[0] = SCREEN_WIDTH / 2;
        y[0] = SCREEN_HEIGHT / 2;        
        for (int i = 1  ; i < x.length; i++) {
            x[i] = -UNIT_SIZE;
            y[i] = -UNIT_SIZE;
        }
        backgroundIndex = 0;
        timer.setDelay(DELAY);
        newApple();
        timer.start();
        if (restartButton != null) {
            this.remove(restartButton);
            restartButton = null;
        }
        if (exitButton != null) {
            this.remove(exitButton);
            exitButton = null;
        }
        this.requestFocusInWindow();
        repaint();      
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
            directionChanged = false;
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (directionChanged) return;
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') direction = 'L';
                        directionChanged = true;
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') direction = 'R';
                        directionChanged = true;
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') direction = 'U';
                        directionChanged = true;
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') direction = 'D';
                      directionChanged = true;
                }
            }
        }
    }
}
*/