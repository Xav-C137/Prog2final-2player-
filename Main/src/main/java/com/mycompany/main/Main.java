/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.main;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Main {
    static final String USERS_FILE = "users.txt";
    static Map<String, Integer> userScores = new LinkedHashMap<>();

    public static void main(String[] args) {
        loadUsers();

        JFrame frame = new JFrame("Bitin ni Luchin");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        Vector<String> users = new Vector<>(userScores.keySet());
        users.add("➕ Create User");
        JComboBox<String> userDropdown = new JComboBox<>(users);
        userDropdown.setMaximumSize(new Dimension(200, 30));
        userDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        userDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Bitin ni Luchin");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 68));

        JButton playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.setMaximumSize(new Dimension(200, 60));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton twoPlayerButton = new JButton("Two Player");
        twoPlayerButton.setFont(new Font("Arial", Font.BOLD, 20));
        twoPlayerButton.setPreferredSize(new Dimension(200, 50));
        twoPlayerButton.setMaximumSize(new Dimension(200, 50));
        twoPlayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        twoPlayerButton.addActionListener(e -> {
        frame.dispose();
        JFrame gameFrame = new JFrame("Bitin ni Luchin - Two Player Mode");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setResizable(false);
        gameFrame.add(new TwoPlayerGamePanel());
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitButton.setMaximumSize(new Dimension(150, 40));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        userDropdown.addActionListener(e -> {
            String selected = (String) userDropdown.getSelectedItem();
            if ("➕ Create User".equals(selected)) {
                String newUser = JOptionPane.showInputDialog(frame, "Enter new username:");
                if (newUser != null && !newUser.trim().isEmpty() && !userScores.containsKey(newUser)) {
                    userScores.put(newUser, 0);
                    saveUsers();
                    users.insertElementAt(newUser, users.size() - 1);
                    userDropdown.setModel(new DefaultComboBoxModel<>(users));
                    userDropdown.setSelectedItem(newUser);
                } else if (userScores.containsKey(newUser)) {
                    JOptionPane.showMessageDialog(frame, "User already exists!");
                }
            }
        });

        playButton.addActionListener(e -> {
            String user = (String) userDropdown.getSelectedItem();
            if (user.equals("➕ Create User") || user == null) {
                JOptionPane.showMessageDialog(frame, "Please select a valid user.");
            } else {
                frame.dispose();
                JFrame gameFrame = new JFrame("Bitin ni Luchin - " + user);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.setResizable(false);
                gameFrame.add(new GamePanel(user));
                gameFrame.pack();
                gameFrame.setLocationRelativeTo(null);
                gameFrame.setVisible(true);
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        // Center Panel for the Main Menu
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(userDropdown);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(playButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(twoPlayerButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(exitButton);
        centerPanel.add(Box.createVerticalGlue());

        // Main Panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Create and add top-left leaderboard panel
        JPanel topLeftPanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftPanelWrapper.setOpaque(false);
        topLeftPanelWrapper.add(createTopScorePanel());
        mainPanel.add(topLeftPanelWrapper, BorderLayout.NORTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Small panel with single top score
    private static JPanel createTopScorePanel() {
        JPanel topScorePanel = new JPanel();
        topScorePanel.setLayout(new BoxLayout(topScorePanel, BoxLayout.Y_AXIS));
        topScorePanel.setBackground(new Color(235, 235, 235));
        topScorePanel.setBorder(BorderFactory.createTitledBorder("🏆 Top Score"));
        topScorePanel.setPreferredSize(new Dimension(180, 80));

        if (!userScores.isEmpty()) {
            Map.Entry<String, Integer> topEntry = Collections.max(
                userScores.entrySet(), Comparator.comparingInt(Map.Entry::getValue)
            );

            JLabel nameLabel = new JLabel(topEntry.getKey());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            JLabel scoreLabel = new JLabel("Score: " + topEntry.getValue());
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            topScorePanel.add(nameLabel);
            topScorePanel.add(scoreLabel);
        } else {
            topScorePanel.add(new JLabel("No scores yet."));
        }

        return topScorePanel;
    }

    public static void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    userScores.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("No user file found. Starting fresh.");
        }
    }

    public static void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, Integer> entry : userScores.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }

    public static void updateScore(String username, int newScore) {
        if (userScores.containsKey(username)) {
            int currentScore = userScores.get(username);
            if (newScore > currentScore) {
                userScores.put(username, newScore);
                saveUsers();
            }
        }
    }
}

/*
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Main {
    static final String USERS_FILE = "users.tx  t";
    static Map<String, Integer> userScores = new LinkedHashMap<>();

    public static void main(String[] args) {
        loadUsers();

        JFrame frame = new JFrame("Bitin ni Luchin");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        Vector<String> users = new Vector<>(userScores.keySet());
        users.add("➕ Create User");
        JComboBox<String> userDropdown = new JComboBox<>(users);
        userDropdown.setMaximumSize(new Dimension(200, 30));
        userDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        userDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Bitin ni Luchin");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 68));

        JButton playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.setMaximumSize(new Dimension(200, 60));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitButton.setMaximumSize(new Dimension(150, 40));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        userDropdown.addActionListener(e -> {
            String selected = (String) userDropdown.getSelectedItem();
            if ("➕ Create User".equals(selected)) {
                String newUser = JOptionPane.showInputDialog(frame, "Enter new username:");
                if (newUser != null && !newUser.trim().isEmpty() && !userScores.containsKey(newUser)) {
                    userScores.put(newUser, 0);
                    saveUsers();
                    users.insertElementAt(newUser, users.size() - 1);
                    userDropdown.setModel(new DefaultComboBoxModel<>(users));
                    userDropdown.setSelectedItem(newUser);
                } else if (userScores.containsKey(newUser)) {
                    JOptionPane.showMessageDialog(frame, "User already exists!");
                }
            }
        });

        playButton.addActionListener(e -> {
            String user = (String) userDropdown.getSelectedItem();
            if (user.equals("➕ Create User") || user == null) {
                JOptionPane.showMessageDialog(frame, "Please select a valid user.");
            } else {
                frame.dispose();
                JFrame gameFrame = new JFrame("Bitin ni Luchin - " + user);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.setResizable(false);
                gameFrame.add(new GamePanel(user));
                gameFrame.pack();
                gameFrame.setLocationRelativeTo(null);
                gameFrame.setVisible(true);
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        // Center Panel for the Main Menu
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(userDropdown);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(playButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(exitButton);
        centerPanel.add(Box.createVerticalGlue());

        // Main Panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Create and add top-left leaderboard panel
        JPanel topLeftPanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftPanelWrapper.setOpaque(false);
        topLeftPanelWrapper.add(createTopScorePanel());
        mainPanel.add(topLeftPanelWrapper, BorderLayout.NORTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Small panel with single top score
    private static JPanel createTopScorePanel() {
        JPanel topScorePanel = new JPanel();
        topScorePanel.setLayout(new BoxLayout(topScorePanel, BoxLayout.Y_AXIS));
        topScorePanel.setBackground(new Color(235, 235, 235));
        topScorePanel.setBorder(BorderFactory.createTitledBorder("🏆 Top Score"));
        topScorePanel.setPreferredSize(new Dimension(180, 80));

        if (!userScores.isEmpty()) {
            Map.Entry<String, Integer> topEntry = Collections.max(
                userScores.entrySet(), Comparator.comparingInt(Map.Entry::getValue)
            );

            JLabel nameLabel = new JLabel(topEntry.getKey());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            JLabel scoreLabel = new JLabel("Score: " + topEntry.getValue());
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            topScorePanel.add(nameLabel);
            topScorePanel.add(scoreLabel);
        } else {
            topScorePanel.add(new JLabel("No scores yet."));
        }

        return topScorePanel;
    }

    public static void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    userScores.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("No user file found. Starting fresh.");
        }
    }

    public static void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, Integer> entry : userScores.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }

    public static void updateScore(String username, int newScore) {
        if (userScores.containsKey(username)) {
            int currentScore = userScores.get(username);
            if (newScore > currentScore) {
                userScores.put(username, newScore);
                saveUsers();
            }
        }
    }
}
*/