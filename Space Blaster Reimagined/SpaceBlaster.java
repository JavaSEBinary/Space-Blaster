// (imports stay the same)
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class SpaceBlaster extends JPanel implements ActionListener, KeyListener {
    Timer timer;
    float playerX = 400;
    int playerY;
    float playerSpeed = 7.5f;
    boolean leftPressed = false, rightPressed = false;

    BufferedImage playerImg, enemyImg, laserImg, backgroundImg, heartImg;
    Clip explosionClip, musicClip;

    ArrayList<Rectangle> lasers = new ArrayList<>();
    ArrayList<Rectangle> enemies = new ArrayList<>();

    int backgroundY = 0;
    int frameCounter = 0;

    int playerHealth = 3;
    int score = 0;
    int highScore = 0;
    long lastHealTime = System.currentTimeMillis();
    boolean isGameOver = false;

    JButton respawnButton;
    JButton quitButton;

    public SpaceBlaster() {
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);

        try {
            playerImg = ImageIO.read(new File("player.png"));
            enemyImg = ImageIO.read(new File("alien.png"));
            laserImg = ImageIO.read(new File("lazer.png"));
            backgroundImg = ImageIO.read(new File("Background.png"));
            heartImg = ImageIO.read(new File("heart.png"));

            AudioInputStream explosionSound = AudioSystem.getAudioInputStream(new File("explode1.wav"));
            explosionClip = AudioSystem.getClip();
            explosionClip.open(explosionSound);

            AudioInputStream musicStream = AudioSystem.getAudioInputStream(new File("Enemy Approaching.wav"));
            musicClip = AudioSystem.getClip();
            musicClip.open(musicStream);
            musicClip.setFramePosition(0);
            musicClip.start();
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Error loading media: " + e.getMessage());
            e.printStackTrace();
        }

        playerY = 600 - 100;

        respawnButton = new JButton("Respawn");
        quitButton = new JButton("Quit Game");
        respawnButton.setBounds(300, 300, 150, 40);
        quitButton.setBounds(300, 360, 150, 40);

        respawnButton.addActionListener(e -> restartGame());
        quitButton.addActionListener(e -> System.exit(0));

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) return;

        if (leftPressed) playerX -= playerSpeed;
        if (rightPressed) playerX += playerSpeed;

        playerX = Math.max(0, Math.min(playerX, getWidth() - 64));

        Iterator<Rectangle> it = lasers.iterator();
        while (it.hasNext()) {
            Rectangle laser = it.next();
            laser.y -= 10;
            if (laser.y < 0) it.remove();
        }

        backgroundY += 4;
        if (backgroundY >= getHeight()) {
            backgroundY = 0;
        }

        frameCounter++;
        if (frameCounter % 120 == 0) {
            int randX = (int)(Math.random() * (getWidth() - 64));
            int randY = 50 + (int)(Math.random() * 200);
            enemies.add(new Rectangle(randX, randY, 64, 64));
        }

        Rectangle playerRect = new Rectangle((int)playerX, playerY, 64, 64);
        Iterator<Rectangle> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Rectangle enemy = enemyIt.next();
            enemy.y += 2;

            if (enemy.y > getHeight()) {
                enemyIt.remove();
                continue;
            }

            for (Rectangle laser : lasers) {
                if (laser.intersects(enemy)) {
                    playExplosion();
                    enemyIt.remove();
                    score += 1;
                    break;
                }
            }

            if (enemy.intersects(playerRect)) {
                playExplosion();
                enemyIt.remove();
                if (playerHealth > 0) {
                    playerHealth -= 1;
                }
            }
        }

        if (System.currentTimeMillis() - lastHealTime >= 600000 && playerHealth < 3) {
            playerHealth++;
            lastHealTime = System.currentTimeMillis();
        }

        if (playerHealth <= 0 && !isGameOver) {
            isGameOver = true;
            if (score > highScore) highScore = score;
            showGameOverMenu();
        }

        repaint();
    }

    private void showGameOverMenu() {
        add(respawnButton);
        add(quitButton);
        respawnButton.setVisible(true);
        quitButton.setVisible(true);
        repaint();
    }

    private void hideGameOverMenu() {
        remove(respawnButton);
        remove(quitButton);
    }

    private void restartGame() {
        playerHealth = 3;
        score = 0;
        lasers.clear();
        enemies.clear();
        playerX = getWidth() / 2f;
        lastHealTime = System.currentTimeMillis();
        isGameOver = false;
        hideGameOverMenu();
        requestFocusInWindow(); // ✅ Fix for macOS keyboard focus
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, backgroundY - getHeight(), getWidth(), getHeight(), null);
            g.drawImage(backgroundImg, 0, backgroundY, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (!isGameOver) {
            g.drawImage(playerImg, (int)playerX, playerY, 64, 64, null);
        }

        for (Rectangle laser : lasers) {
            g.drawImage(laserImg, laser.x, laser.y, 8, 24, null);
        }

        for (Rectangle enemy : enemies) {
            g.drawImage(enemyImg, enemy.x, enemy.y, 96, 96, null);
        }

        if (heartImg != null) {
            for (int i = 0; i < playerHealth; i++) {
                g.drawImage(heartImg, 20 + i * 34, 20, 28, 28, null);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 60);

        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", getWidth() / 2 - 160, getHeight() / 2 - 120);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Your Score: " + score, getWidth() / 2 - 80, getHeight() / 2 - 60);
            g.drawString("High Score: " + highScore, getWidth() / 2 - 85, getHeight() / 2 - 30);
        }
    }

    public void fireLaser() {
        lasers.add(new Rectangle((int)playerX + 64 / 2 - 4, playerY, 8, 24));
    }

    public void playExplosion() {
        if (explosionClip != null) {
            explosionClip.setFramePosition(0);
            explosionClip.start();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !isGameOver) fireLaser();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Blaster");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        frame.setUndecorated(true);
        gd.setFullScreenWindow(frame);

        SpaceBlaster game = new SpaceBlaster();
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        game.requestFocusInWindow(); // ✅ Fix for macOS keyboard input
    }
}
