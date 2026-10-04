package racing.ui;

import racing.exception.CollisionException;
import racing.model.CoinItem;
import racing.model.DifficultyLevel;
import racing.model.Obstacle;
import racing.service.GameEngine;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener {
    private final GameEngine engine;
    private final Timer timer;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;

    public GamePanel(DifficultyLevel difficulty) {
        this.engine = new GameEngine(difficulty);
        this.timer = new Timer(16, this); // ~60 FPS
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (engine.isGameOver()) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER) {
                        restartRace();
                    }
                    return;
                }

                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    leftPressed = true;
                }
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    rightPressed = true;
                }
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W || code == KeyEvent.VK_SPACE) {
                    upPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    leftPressed = false;
                }
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    rightPressed = false;
                }
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W || code == KeyEvent.VK_SPACE) {
                    upPressed = false;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (engine.isGameOver()) {
                    restartRace();
                }
            }
        });

        timer.start();
    }

    private void restartRace() {
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        engine.restart();
        if (!timer.isRunning()) {
            timer.start();
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!engine.isGameOver()) {
            if (leftPressed) {
                engine.getPlayer().steerLeft();
            }
            if (rightPressed) {
                engine.getPlayer().steerRight();
            }
            engine.getPlayer().setNitroActive(upPressed);

            try {
                engine.update();
            } catch (CollisionException ex) {
                System.err.println("[NFS LOG] " + ex.getMessage());
                timer.stop();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isNitro = engine.getPlayer().isNitroActive();

        // 1. Ночной город / Фон с эффектом скорости
        g2d.setColor(new Color(10, 5, 20));
        g2d.fillRect(0, 0, 400, 600);

        // Огни ночного города по бокам
        g2d.setColor(new Color(255, 0, 128, 60));
        g2d.fillRect(0, 0, 80, 600);
        g2d.setColor(new Color(0, 200, 255, 60));
        g2d.fillRect(320, 0, 80, 600);

        // 2. Мокрый темный асфальт
        g2d.setColor(new Color(25, 25, 30));
        g2d.fillRect(80, 0, 240, 600);

        // 3. Неоновые боковые отбойники
        int offsetY = engine.getTrackOffsetY();
        g2d.setColor(new Color(255, 0, 100));
        g2d.fillRect(76, 0, 4, 600);
        g2d.setColor(new Color(0, 220, 255));
        g2d.fillRect(320, 0, 4, 600);

        // 4. Полосы разметки
        g2d.setColor(isNitro ? new Color(0, 255, 255, 200) : new Color(200, 200, 200, 150));
        for (int i = -40; i < 600; i += 50) {
            int lineLen = isNitro ? 35 : 20;
            g2d.fillRect(160, i + offsetY, 4, lineLen);
            g2d.fillRect(230, i + offsetY, 4, lineLen);
        }

        // 5. Отрисовка объектов
        for (CoinItem coin : engine.getCoins()) coin.draw(g2d);
        for (Obstacle obs : engine.getObstacles()) obs.draw(g2d);
        engine.getPlayer().draw(g2d);

        // 6. NFS Спидометр и HUD
        drawNfsHUD(g2d);

        // 7. Экран Game Over
        if (engine.isGameOver()) {
            g2d.setColor(new Color(15, 0, 20, 230));
            g2d.fillRect(0, 0, 400, 600);

            g2d.setColor(new Color(255, 0, 80));
            g2d.setFont(new Font("Impact", Font.ITALIC, 46));
            g2d.drawString("BUSTED!", 120, 250);

            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("SCORE: " + engine.getTotalScore(), 145, 295);

            g2d.setColor(new Color(0, 240, 255));
            g2d.fillRect(70, 340, 260, 45);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(70, 340, 260, 45);

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2d.drawString("Нажмите ПРОБЕЛ / R", 115, 368);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("(или кликните мышкой для рестарта)", 95, 410);
        }
    }

    private void drawNfsHUD(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillOval(260, 460, 120, 120);
        g2d.setColor(new Color(0, 240, 255));
        g2d.drawOval(260, 460, 120, 120);

        int displaySpeed = (engine.getDifficulty().getSpeedMultiplier() * 18) + (engine.getPlayer().isNitroActive() ? 85 : 0);
        g2d.setFont(new Font("Impact", Font.ITALIC, 28));
        g2d.setColor(engine.getPlayer().isNitroActive() ? Color.CYAN : Color.WHITE);
        g2d.drawString(String.valueOf(displaySpeed), 300, 520);

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.setColor(Color.GRAY);
        g2d.drawString("KM/H", 307, 535);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(15, 540, 120, 16);

        int nitroWidth = (int) (120 * (engine.getPlayer().getNitroAmount() / 100.0));
        g2d.setColor(new Color(0, 200, 255));
        g2d.fillRect(15, 540, nitroWidth, 16);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(15, 540, 120, 16);
        g2d.setFont(new Font("Impact", Font.PLAIN, 12));
        g2d.drawString("NOS (HOLD UP / SPACE)", 15, 533);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("SCORE: " + engine.getTotalScore(), 15, 25);

        g2d.setColor(new Color(255, 0, 80));
        g2d.fillRect(15, 35, (int)(100 * (engine.getPlayer().getHealth() / 100.0)), 8);
    }
}
