package com.snake;

import com.snake.ui.GamePanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Точка входа в программу.
 * Инициализирует окно приложения JFrame.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Змейка (Snake Game) - Основы Java");
            GamePanel gamePanel = new GamePanel();

            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
