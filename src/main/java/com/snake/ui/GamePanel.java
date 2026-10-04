package com.snake.ui;

import com.snake.exception.GameOverException;
import com.snake.model.Direction;
import com.snake.model.Food;
import com.snake.model.Snake;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Игровой панель UI на основе Swing JPanel.
 */
public class GamePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int SCREEN_WIDTH = GRID_WIDTH * TILE_SIZE;
    private static final int SCREEN_HEIGHT = GRID_HEIGHT * TILE_SIZE;
    private static final int DELAY = 110;

    private Snake snake;
    private Food food;
    private Timer timer;
    private boolean isGameOver;
    private int score;
    private String gameOverReason;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(14, 22, 16));
        setFocusable(true);
        addKeyListener(new GameKeyAdapter());
        initGame();
    }

    private void initGame() {
        snake = new Snake(GRID_WIDTH / 2, GRID_HEIGHT / 2, GRID_WIDTH, GRID_HEIGHT);
        food = new Food();
        spawnFoodValid();
        isGameOver = false;
        score = 0;
        gameOverReason = "";

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    private void spawnFoodValid() {
        do {
            food.spawn(GRID_WIDTH, GRID_HEIGHT);
        } while (snake.getBody().contains(food.getPosition()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver) {
            try {
                snake.move();

                if (snake.getHead().equals(food.getPosition())) {
                    snake.grow();
                    score += 10;
                    spawnFoodValid();
                }

            } catch (GameOverException ex) {
                isGameOver = true;
                gameOverReason = ex.getMessage();
                timer.stop();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Тёмно-зеленая шахматная трава / фон игрового поля
        for (int r = 0; r < GRID_HEIGHT; r++) {
            for (int c = 0; c < GRID_WIDTH; c++) {
                if ((r + c) % 2 == 0) {
                    g.setColor(new Color(18, 28, 20));
                } else {
                    g.setColor(new Color(14, 22, 16));
                }
                g.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        if (!isGameOver) {
            food.draw(g, TILE_SIZE);
            snake.draw(g, TILE_SIZE);

            // Отрисовка счета
            g.setColor(new Color(240, 240, 240));
            g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g.drawString("🍎 Яблоки: " + (score / 10) + "  |  Счёт: " + score, 12, 22);
        } else {
            drawGameOverScreen(g);
        }
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(231, 76, 60));
        g.setFont(new Font("Segoe UI", Font.BOLD, 30));
        FontMetrics fm1 = getFontMetrics(g.getFont());
        String title = "GAME OVER";
        g.drawString(title, (SCREEN_WIDTH - fm1.stringWidth(title)) / 2, SCREEN_HEIGHT / 3);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        FontMetrics fm2 = getFontMetrics(g.getFont());

        String reasonStr = "Причина: " + gameOverReason;
        g.drawString(reasonStr, (SCREEN_WIDTH - fm2.stringWidth(reasonStr)) / 2, SCREEN_HEIGHT / 2);

        String scoreStr = "Итоговый счёт: " + score;
        g.drawString(scoreStr, (SCREEN_WIDTH - fm2.stringWidth(scoreStr)) / 2, SCREEN_HEIGHT / 2 + 30);

        g.setColor(new Color(241, 196, 15));
        g.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics fm3 = getFontMetrics(g.getFont());
        String restartStr = "Нажмите ПРОБЕЛ для новой игры";
        g.drawString(restartStr, (SCREEN_WIDTH - fm3.stringWidth(restartStr)) / 2, SCREEN_HEIGHT / 2 + 75);
    }

    private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (isGameOver && key == KeyEvent.VK_SPACE) {
                initGame();
                repaint();
                return;
            }

            switch (key) {
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> snake.setDirection(Direction.LEFT);
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> snake.setDirection(Direction.RIGHT);
                case KeyEvent.VK_UP, KeyEvent.VK_W -> snake.setDirection(Direction.UP);
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> snake.setDirection(Direction.DOWN);
            }
        }
    }
}
