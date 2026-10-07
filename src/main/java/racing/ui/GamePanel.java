package racing.ui;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import racing.exception.CollisionException;
import racing.model.CoinItem;
import racing.model.DifficultyLevel;
import racing.model.Obstacle;
import racing.service.GameEngine;

public class GamePanel extends StackPane {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    private final GameEngine engine;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private AnimationTimer gameLoop;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;

    public GamePanel(DifficultyLevel difficulty) {
        this.engine = new GameEngine(difficulty);
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        getChildren().add(canvas);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #0a0514;");

        setFocusTraversable(true);

        setOnKeyPressed(this::handleKeyPressed);
        setOnKeyReleased(this::handleKeyReleased);
        setOnMouseClicked(e -> {
            if (engine.isGameOver()) {
                restartRace();
            }
        });

        initGameLoop();
    }

    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
                render();
            }
        };
        gameLoop.start();
    }

    private void handleKeyPressed(KeyEvent e) {
        if (engine.isGameOver()) {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.R || e.getCode() == KeyCode.ENTER) {
                restartRace();
            }
            return;
        }

        KeyCode code = e.getCode();
        if (code == KeyCode.LEFT || code == KeyCode.A) {
            leftPressed = true;
        }
        if (code == KeyCode.RIGHT || code == KeyCode.D) {
            rightPressed = true;
        }
        if (code == KeyCode.UP || code == KeyCode.W || code == KeyCode.SPACE) {
            upPressed = true;
        }
    }

    private void handleKeyReleased(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.LEFT || code == KeyCode.A) {
            leftPressed = false;
        }
        if (code == KeyCode.RIGHT || code == KeyCode.D) {
            rightPressed = false;
        }
        if (code == KeyCode.UP || code == KeyCode.W || code == KeyCode.SPACE) {
            upPressed = false;
        }
    }

    private void restartRace() {
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        engine.restart();
    }

    private void updateGame() {
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
            }
        }
    }

    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void render() {
        boolean isNitro = engine.getPlayer().isNitroActive();

        // 1. Ночной город / Фон с эффектом скорости
        gc.setFill(Color.rgb(10, 5, 20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Огни ночного города по бокам
        gc.setFill(Color.rgb(255, 0, 128, 60 / 255.0));
        gc.fillRect(0, 0, 80, HEIGHT);
        gc.setFill(Color.rgb(0, 200, 255, 60 / 255.0));
        gc.fillRect(320, 0, 80, HEIGHT);

        // 2. Мокрый темный асфальт
        gc.setFill(Color.rgb(25, 25, 30));
        gc.fillRect(80, 0, 240, HEIGHT);

        // 3. Неоновые боковые отбойники
        int offsetY = engine.getTrackOffsetY();
        gc.setFill(Color.rgb(255, 0, 100));
        gc.fillRect(76, 0, 4, HEIGHT);
        gc.setFill(Color.rgb(0, 220, 255));
        gc.fillRect(320, 0, 4, HEIGHT);

        // 4. Полосы разметки
        gc.setFill(isNitro ? Color.rgb(0, 255, 255, 200 / 255.0) : Color.rgb(200, 200, 200, 150 / 255.0));
        for (int i = -40; i < HEIGHT; i += 50) {
            int lineLen = isNitro ? 35 : 20;
            gc.fillRect(160, i + offsetY, 4, lineLen);
            gc.fillRect(230, i + offsetY, 4, lineLen);
        }

        // 5. Отрисовка объектов
        for (CoinItem coin : engine.getCoins()) coin.draw(gc);
        for (Obstacle obs : engine.getObstacles()) obs.draw(gc);
        engine.getPlayer().draw(gc);

        // 6. NFS Спидометр и HUD
        drawNfsHUD(gc);

        // 7. Экран Game Over
        if (engine.isGameOver()) {
            gc.setFill(Color.rgb(15, 0, 20, 230 / 255.0));
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            gc.setFill(Color.rgb(255, 0, 80));
            gc.setFont(Font.font("Impact", FontPosture.ITALIC, 46));
            gc.fillText("BUSTED!", 120, 250);

            gc.setFill(Color.CYAN);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            gc.fillText("SCORE: " + engine.getTotalScore(), 145, 295);

            gc.setFill(Color.rgb(0, 240, 255));
            gc.fillRect(70, 340, 260, 45);
            gc.setStroke(Color.WHITE);
            gc.strokeRect(70, 340, 260, 45);

            gc.setFill(Color.rgb(20, 20, 30));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.fillText("Нажмите ПРОБЕЛ / R", 115, 368);

            gc.setFont(Font.font("Segoe UI", 12));
            gc.setFill(Color.rgb(200, 200, 200));
            gc.fillText("(или кликните мышкой для рестарта)", 95, 410);
        }
    }

    private void drawNfsHUD(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 180 / 255.0));
        gc.fillOval(260, 460, 120, 120);
        gc.setStroke(Color.rgb(0, 240, 255));
        gc.strokeOval(260, 460, 120, 120);

        int displaySpeed = (engine.getDifficulty().getSpeedMultiplier() * 18) + (engine.getPlayer().isNitroActive() ? 85 : 0);
        gc.setFont(Font.font("Impact", FontPosture.ITALIC, 28));
        gc.setFill(engine.getPlayer().isNitroActive() ? Color.CYAN : Color.WHITE);
        gc.fillText(String.valueOf(displaySpeed), 300, 520);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setFill(Color.GRAY);
        gc.fillText("KM/H", 307, 535);

        gc.setFill(Color.BLACK);
        gc.fillRect(15, 540, 120, 16);

        int nitroWidth = (int) (120 * (engine.getPlayer().getNitroAmount() / 100.0));
        gc.setFill(Color.rgb(0, 200, 255));
        gc.fillRect(15, 540, nitroWidth, 16);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(15, 540, 120, 16);
        gc.setFont(Font.font("Impact", 12));
        gc.setFill(Color.WHITE);
        gc.fillText("NOS (HOLD UP / SPACE)", 15, 533);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.YELLOW);
        gc.fillText("SCORE: " + engine.getTotalScore(), 15, 25);

        gc.setFill(Color.rgb(255, 0, 80));
        gc.fillRect(15, 35, (int)(100 * (engine.getPlayer().getHealth() / 100.0)), 8);
    }

    public static void showGame(DifficultyLevel difficulty, Stage parent) {
        Stage stage = new Stage();
        stage.setTitle("Java Racing Game - " + difficulty.name());
        GamePanel panel = new GamePanel(difficulty);
        Scene scene = new Scene(panel, WIDTH, HEIGHT);
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
