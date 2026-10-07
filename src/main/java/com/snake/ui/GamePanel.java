package com.snake.ui;

import com.snake.exception.GameOverException;
import com.snake.model.Direction;
import com.snake.model.Food;
import com.snake.model.Snake;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Игровая панель UI для Змейки на основе JavaFX Canvas & StackPane.
 */
public class GamePanel extends StackPane {
    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int SCREEN_WIDTH = GRID_WIDTH * TILE_SIZE;
    private static final int SCREEN_HEIGHT = GRID_HEIGHT * TILE_SIZE;
    private static final int DELAY_MS = 110;

    private final Canvas canvas;
    private final GraphicsContext gc;

    private Snake snake;
    private Food food;
    private Timeline timeline;
    private boolean isGameOver;
    private int score;
    private String gameOverReason;

    public GamePanel() {
        this.canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        getChildren().add(canvas);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #0e1610;");

        setFocusTraversable(true);
        setOnKeyPressed(this::handleKeyPressed);

        initGame();
    }

    private void initGame() {
        snake = new Snake(GRID_WIDTH / 2, GRID_HEIGHT / 2, GRID_WIDTH, GRID_HEIGHT);
        food = new Food();
        spawnFoodValid();
        isGameOver = false;
        score = 0;
        gameOverReason = "";

        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.millis(DELAY_MS), e -> gameTick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        render();
    }

    private void spawnFoodValid() {
        do {
            food.spawn(GRID_WIDTH, GRID_HEIGHT);
        } while (snake.getBody().contains(food.getPosition()));
    }

    private void gameTick() {
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
                timeline.stop();
            }
        }
        render();
    }

    public void stopGame() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void render() {
        // 1. Тёмно-зеленая шахматная трава / фон игрового поля
        for (int r = 0; r < GRID_HEIGHT; r++) {
            for (int c = 0; c < GRID_WIDTH; c++) {
                if ((r + c) % 2 == 0) {
                    gc.setFill(Color.rgb(18, 28, 20));
                } else {
                    gc.setFill(Color.rgb(14, 22, 16));
                }
                gc.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        if (!isGameOver) {
            food.draw(gc, TILE_SIZE);
            snake.draw(gc, TILE_SIZE);

            // Отрисовка счета
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.BASELINE);
            gc.setFill(Color.rgb(240, 240, 240));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.fillText("🍎 Яблоки: " + (score / 10) + "  |  Счёт: " + score, 12, 22);
        } else {
            drawGameOverScreen();
        }
    }

    private void drawGameOverScreen() {
        gc.setFill(Color.rgb(0, 0, 0, 190 / 255.0));
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFill(Color.rgb(231, 76, 60));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        gc.fillText("GAME OVER", SCREEN_WIDTH / 2.0, SCREEN_HEIGHT / 3.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        gc.fillText("Причина: " + gameOverReason, SCREEN_WIDTH / 2.0, SCREEN_HEIGHT / 2.0);

        gc.fillText("Итоговый счёт: " + score, SCREEN_WIDTH / 2.0, SCREEN_HEIGHT / 2.0 + 30);

        gc.setFill(Color.rgb(241, 196, 15));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.fillText("Нажмите ПРОБЕЛ для новой игры", SCREEN_WIDTH / 2.0, SCREEN_HEIGHT / 2.0 + 75);
    }

    private void handleKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();

        if (isGameOver && key == KeyCode.SPACE) {
            initGame();
            return;
        }

        switch (key) {
            case LEFT, A -> snake.setDirection(Direction.LEFT);
            case RIGHT, D -> snake.setDirection(Direction.RIGHT);
            case UP, W -> snake.setDirection(Direction.UP);
            case DOWN, S -> snake.setDirection(Direction.DOWN);
            default -> {}
        }
    }

    public static void showGame(Stage parent) {
        Stage stage = new Stage();
        stage.setTitle("Змейка (Snake Game)");
        GamePanel panel = new GamePanel();
        Scene scene = new Scene(panel, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> panel.stopGame());
        if (parent != null) {
            stage.initOwner(parent);
        }
        stage.show();
        panel.requestFocus();
    }
}
