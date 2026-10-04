package com.snake.ui;

import com.snake.exception.GameOverException;
import com.snake.model.Direction;
import com.snake.model.Food;
import com.snake.model.Point;
import com.snake.model.Snake;

import java.util.Scanner;

/**
 * Консольная версия (CLI) игры «Змейка».
 * Позволяет играть в змейку в текстовом терминале.
 */
public class ConsoleSnakeGame {
    private static final int GRID_WIDTH = 12;
    private static final int GRID_HEIGHT = 12;

    private Snake snake;
    private Food food;
    private int score;
    private boolean isGameOver;
    private String gameOverReason;
    private final Scanner scanner;

    public ConsoleSnakeGame() {
        this.scanner = new Scanner(System.in);
        initGame();
    }

    private void initGame() {
        this.snake = new Snake(GRID_WIDTH / 2, GRID_HEIGHT / 2, GRID_WIDTH, GRID_HEIGHT);
        this.food = new Food();
        spawnFoodValid();
        this.score = 0;
        this.isGameOver = false;
        this.gameOverReason = "";
    }

    private void spawnFoodValid() {
        do {
            food.spawn(GRID_WIDTH, GRID_HEIGHT);
        } while (snake.getBody().contains(food.getPosition()));
    }

    public void start() {
        System.out.println("\n==========================================");
        System.out.println("   🐍  SNAKE GAME (CONSOLE CLI)");
        System.out.println("==========================================");

        while (true) {
            printGrid();

            if (isGameOver) {
                System.out.println("\n💀 GAME OVER! 💀");
                System.out.println("Причина: " + gameOverReason);
                System.out.println("Итоговый счёт: " + score + " (Съедено яблок: " + (score / 10) + ")");
                System.out.println("\nВыберите действие:");
                System.out.println("  r — Начать заново (РЕСТАРТ)");
                System.out.println("  q — Выйти в главное меню");
                System.out.print("Выбор > ");

                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("r")) {
                    initGame();
                    continue;
                } else {
                    System.out.println("Возврат в меню...");
                    return;
                }
            }

            System.out.print("Управление (w-вверх, s-вниз, a-влево, d-вправо, r-рестарт, q-выход, Enter-шаг) > ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("q")) {
                System.out.println("Выход из змейки...");
                return;
            } else if (input.equals("r")) {
                initGame();
                continue;
            } else if (input.equals("w")) {
                snake.setDirection(Direction.UP);
            } else if (input.equals("s")) {
                snake.setDirection(Direction.DOWN);
            } else if (input.equals("a")) {
                snake.setDirection(Direction.LEFT);
            } else if (input.equals("d")) {
                snake.setDirection(Direction.RIGHT);
            }

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
            }
        }
    }

    private void printGrid() {
        System.out.println("\n-------------------------------------------------");
        System.out.printf("🍎 Яблоки: %2d  |  СЧЁТ: %4d  |  Длина: %2d\n", (score / 10), score, snake.getBody().size());
        System.out.println("-------------------------------------------------");

        char[][] board = new char[GRID_HEIGHT][GRID_WIDTH];
        for (int r = 0; r < GRID_HEIGHT; r++) {
            for (int c = 0; c < GRID_WIDTH; c++) {
                board[r][c] = '.';
            }
        }

        // Яблоко
        Point foodPos = food.getPosition();
        if (foodPos.getY() >= 0 && foodPos.getY() < GRID_HEIGHT && foodPos.getX() >= 0 && foodPos.getX() < GRID_WIDTH) {
            board[foodPos.getY()][foodPos.getX()] = '@';
        }

        // Тело змейки
        for (int i = 1; i < snake.getBody().size(); i++) {
            Point p = snake.getBody().get(i);
            if (p.getY() >= 0 && p.getY() < GRID_HEIGHT && p.getX() >= 0 && p.getX() < GRID_WIDTH) {
                board[p.getY()][p.getX()] = 'o';
            }
        }

        // Голова змейки
        Point head = snake.getHead();
        if (head.getY() >= 0 && head.getY() < GRID_HEIGHT && head.getX() >= 0 && head.getX() < GRID_WIDTH) {
            board[head.getY()][head.getX()] = 'O';
        }

        // Отрисовка с рамкой
        System.out.print("+");
        for (int c = 0; c < GRID_WIDTH; c++) System.out.print("--");
        System.out.println("+");

        for (int r = 0; r < GRID_HEIGHT; r++) {
            System.out.print("|");
            for (int c = 0; c < GRID_WIDTH; c++) {
                char ch = board[r][c];
                if (ch == '@') {
                    System.out.print("🍎");
                } else if (ch == 'O') {
                    System.out.print("O ");
                } else if (ch == 'o') {
                    System.out.print("o ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println("|");
        }

        System.out.print("+");
        for (int c = 0; c < GRID_WIDTH; c++) System.out.print("--");
        System.out.println("+");
    }
}
