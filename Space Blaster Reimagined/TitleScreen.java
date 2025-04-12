import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

public class TitleScreen {
    private static Clip clip;

    public static void main(String[] args) {
        // Setup JFrame
        JFrame frame = new JFrame("Space Blaster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center window

        // Load background image
        Image backgroundImg = new ImageIcon("Background.png").getImage();

        // Create a custom JPanel to draw the scaled background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));

        // Title label
        JLabel title = new JLabel("Space Blaster", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Spacer
        backgroundPanel.add(Box.createVerticalStrut(60));
        backgroundPanel.add(title);
        backgroundPanel.add(Box.createVerticalStrut(50));

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // So background shows through
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton startButton = new JButton("Start Game");
        JButton quitButton = new JButton("Quit Game");

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button actions
        startButton.addActionListener(e -> {
            stopMusic();
            frame.dispose();
            SpaceBlaster.main(null); // Launch game
        });

        quitButton.addActionListener(e -> {
            stopMusic();
            System.exit(0);
        });

        buttonPanel.add(startButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(quitButton);

        backgroundPanel.add(buttonPanel);
        frame.setContentPane(backgroundPanel);
        frame.setVisible(true);

        playMusic("Enemy Approaching.wav"); // Play music
    }

    private static void playMusic(String filepath) {
        try {
            File musicPath = new File(filepath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Music file not found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}
