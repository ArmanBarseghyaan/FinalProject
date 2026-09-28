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
 * Демонстрирует перехват и обработку исключений (try-catch),
 * использование игрового цикла и отрисовку интерфейсов.
 */
public class GamePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int SCREEN_WIDTH = GRID_WIDTH * TILE_SIZE;
    private static final int SCREEN_HEIGHT = GRID_HEIGHT * TILE_SIZE;
    private static final int DELAY = 120;

    private Snake snake;
    private Food food;
    private Timer timer;
    private boolean isGameOver;
    private int score;
    private String gameOverReason;

    // Конструктор
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new GameKeyAdapter());
        initGame();
    }

    /**
     * Инициализация или сброс состояния игры.
     */
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
                // Пытаемся сделать ход
                snake.move();

                // Проверка поедания еды
                if (snake.getHead().equals(food.getPosition())) {
                    snake.grow();
                    score += 10;
                    spawnFoodValid();
                }

            } catch (GameOverException ex) {
                // ОБРАБОТКА ИСКЛЮЧЕНИЯ (Exception handling)
                isGameOver = true;
                gameOverReason = ex.getMessage();
                timer.stop();
                System.out.println("Исключение обработано: " + ex.getMessage());
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isGameOver) {
            // Отрисовка еды и змейки (используя контракт Drawable)
            food.draw(g, TILE_SIZE);
            snake.draw(g, TILE_SIZE);

            // Отрисовка счета
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Счет: " + score, 10, 20);
        } else {
            drawGameOverScreen(g);
        }
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm1 = getFontMetrics(g.getFont());
        String title = "GAME OVER";
        g.drawString(title, (SCREEN_WIDTH - fm1.stringWidth(title)) / 2, SCREEN_HEIGHT / 3);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fm2 = getFontMetrics(g.getFont());

        String reasonStr = "Причина: " + gameOverReason;
        g.drawString(reasonStr, (SCREEN_WIDTH - fm2.stringWidth(reasonStr)) / 2, SCREEN_HEIGHT / 2);

        String scoreStr = "Итоговый счет: " + score;
        g.drawString(scoreStr, (SCREEN_WIDTH - fm2.stringWidth(scoreStr)) / 2, SCREEN_HEIGHT / 2 + 30);

        g.setColor(Color.YELLOW);
        String restartStr = "Нажмите ПРОБЕЛ для перезапуска";
        g.drawString(restartStr, (SCREEN_WIDTH - fm2.stringWidth(restartStr)) / 2, SCREEN_HEIGHT / 2 + 70);
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
