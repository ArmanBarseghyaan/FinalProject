package finalproject.pacman.ui;

import finalproject.pacman.audio.SoundManager;
import finalproject.pacman.model.Direction;
import finalproject.pacman.model.Ghost;
import finalproject.pacman.model.Maze;
import finalproject.pacman.model.Pacman;
import javafx.animation.AnimationTimer;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Игровая JavaFX-панель Pacman. Таймер обновляет состояние примерно 60 раз в секунду.
 */
public final class PacmanGamePanel extends StackPane {
    private static final long FRIGHTENED_DURATION_NANOS = 7_000_000_000L;
    private static final int GHOST_HOUSE_ROW = 15;
    private static final Color BACKGROUND = Color.rgb(10, 13, 31);
    private static final Color WALL_COLOR = Color.rgb(27, 71, 166);

    private final Maze maze = new Maze();
    private final Pacman pacman = new Pacman(1, 1);
    private final List<Ghost> ghosts = new ArrayList<>();
    private final SoundManager sounds = new SoundManager();
    private final Canvas canvas;
    private final GraphicsContext gc;

    private AnimationTimer gameTimer;
    private int score;
    private int lives = 3;
    private boolean gameOver;
    private boolean won;
    private boolean paused;
    private boolean started;
    private long frightenedUntil;
    private int ghostsEaten;

    public PacmanGamePanel() {
        this.canvas = new Canvas(maze.getWidth(), maze.getHeight());
        this.gc = canvas.getGraphicsContext2D();

        getChildren().add(canvas);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #0a0d1f;");

        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 12, Color.rgb(245, 73, 96)));
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 13, Color.rgb(70, 222, 235)));
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 14, Color.rgb(255, 154, 65)));
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 15, Color.rgb(220, 105, 235)));

        setFocusTraversable(true);
        setOnKeyPressed(this::handleKeyPressed);

        initTimer();
        render();
    }

    private void initTimer() {
        gameTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_000_000L) {
                    updateGame();
                    render();
                    lastUpdate = now;
                }
            }
        };
        gameTimer.start();
    }

    private void handleKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();

        if (code == KeyCode.P) {
            if (started && !gameOver) {
                paused = !paused;
                if (paused) {
                    sounds.pause();
                } else {
                    sounds.resume();
                }
                render();
            }
            return;
        }

        if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            startOrRestartGame();
            return;
        }

        if (!gameOver && !paused) {
            switch (code) {
                case LEFT, A -> pacman.requestDirection(Direction.LEFT);
                case RIGHT, D -> pacman.requestDirection(Direction.RIGHT);
                case UP, W -> pacman.requestDirection(Direction.UP);
                case DOWN, S -> pacman.requestDirection(Direction.DOWN);
                default -> {}
            }
        }
    }

    private void updateGame() {
        if (!started || gameOver || paused) {
            return;
        }

        pacman.move(maze);
        int row = pacman.getRow(maze);
        int column = pacman.getColumn(maze);
        if (maze.collectPellet(row, column)) {
            score += 10;
            sounds.playPellet();
        }
        if (maze.collectEnergizer(row, column)) {
            score += 50;
            frightenedUntil = System.nanoTime() + FRIGHTENED_DURATION_NANOS;
            ghostsEaten = 0;
            sounds.playEnergizer();
        }
        boolean frightened = isFrightened();
        for (Ghost ghost : ghosts) {
            ghost.setFrightened(frightened);
        }
        if (maze.getRemainingPellets() == 0) {
            won = true;
            gameOver = true;
            sounds.stopAll();
            return;
        }

        for (Ghost ghost : ghosts) {
            ghost.chase(maze, pacman);
        }
        checkCollisions();
        if (!isFrightened()) {
            for (Ghost ghost : ghosts) {
                ghost.setFrightened(false);
            }
        }
    }

    private boolean isFrightened() {
        return System.nanoTime() < frightenedUntil;
    }

    private void checkCollisions() {
        double collisionDistance = Maze.TILE_SIZE * 0.62;
        for (Ghost ghost : ghosts) {
            double dx = ghost.getX() - pacman.getX();
            double dy = ghost.getY() - pacman.getY();
            if (dx * dx + dy * dy < collisionDistance * collisionDistance) {
                if (ghost.isFrightened()) {
                    score += 200 << Math.min(ghostsEaten, 3);
                    ghostsEaten++;
                    ghost.resetPosition();
                    sounds.playGhostEaten();
                    continue;
                }
                sounds.playDeath();
                lives--;
                if (lives == 0) {
                    gameOver = true;
                    won = false;
                    sounds.stopAll();
                } else {
                    resetPositions();
                }
                return;
            }
        }
    }

    private void resetPositions() {
        pacman.resetForNewLife();
        for (Ghost ghost : ghosts) {
            ghost.resetPosition();
        }
    }

    public void restartGame() {
        maze.resetCollectibles();
        pacman.resetForNewLife();
        for (Ghost ghost : ghosts) {
            ghost.resetPosition();
        }
        score = 0;
        lives = 3;
        gameOver = false;
        won = false;
        paused = false;
        started = true;
        frightenedUntil = 0L;
        ghostsEaten = 0;
        for (Ghost ghost : ghosts) {
            ghost.setFrightened(false);
        }
        sounds.startGame();
    }

    private void startOrRestartGame() {
        if (!started || gameOver || won) {
            restartGame();
        }
    }

    public void stopGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        sounds.close();
    }

    public double getCanvasWidth() {
        return canvas.getWidth();
    }

    public double getCanvasHeight() {
        return canvas.getHeight();
    }

    private void render() {
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, maze.getWidth(), maze.getHeight());

        drawMaze();
        drawStatus();
        pacman.draw(gc);
        for (Ghost ghost : ghosts) {
            ghost.draw(gc);
        }

        if (!started) {
            drawOverlay("ПАКМАН", "Нажмите Enter или пробел, чтобы начать");
        } else if (paused || gameOver) {
            drawOverlay(paused ? "ПАУЗА" : (won ? "ПОБЕДА!" : "ИГРА ОКОНЧЕНА"),
                    paused ? "Нажмите P, чтобы продолжить"
                            : "Нажмите Enter или пробел, чтобы начать заново");
        }
    }

    private void drawMaze() {
        for (int row = 0; row < Maze.ROWS; row++) {
            for (int column = 0; column < Maze.COLUMNS; column++) {
                int x = column * Maze.TILE_SIZE;
                int y = row * Maze.TILE_SIZE;
                if (!maze.isWalkable(row, column)) {
                    gc.setFill(WALL_COLOR);
                    gc.fillRoundRect(x + 2, y + 2, Maze.TILE_SIZE - 4,
                            Maze.TILE_SIZE - 4, 8, 8);
                    gc.setStroke(Color.rgb(54, 116, 225));
                    gc.setLineWidth(1.2);
                    gc.strokeRoundRect(x + 4, y + 4, Maze.TILE_SIZE - 8,
                            Maze.TILE_SIZE - 8, 6, 6);
                } else if (maze.hasEnergizer(row, column)) {
                    gc.setFill(Color.rgb(255, 236, 190));
                    int size = 14;
                    int inset = (Maze.TILE_SIZE - size) / 2;
                    gc.fillOval(x + inset, y + inset, size, size);
                } else if (maze.hasPellet(row, column)) {
                    gc.setFill(Color.rgb(255, 224, 170));
                    int dotSize = 5;
                    gc.fillOval(x + (Maze.TILE_SIZE - dotSize) / 2.0,
                            y + (Maze.TILE_SIZE - dotSize) / 2.0, dotSize, dotSize);
                }
                if (maze.isGhostDoor(row, column)) {
                    gc.setFill(Color.rgb(255, 170, 205));
                    gc.fillRect(x + 2, y + Maze.TILE_SIZE / 2.0 - 2,
                            Maze.TILE_SIZE - 4, 4);
                }
            }
        }
    }

    private void drawStatus() {
        gc.setFill(Color.rgb(10, 13, 31, 210 / 255.0));
        gc.fillRect(0, 0, maze.getWidth(), 30);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
        gc.fillText("Счёт: " + score + "     Жизни: " + lives
                + "     Точки: " + maze.getRemainingPellets()
                + "     Стрелки/WASD — движение   P — пауза",
                10, 20);
    }

    private void drawOverlay(String title, String hint) {
        gc.setFill(Color.rgb(0, 0, 0, 185 / 255.0));
        gc.fillRect(0, 0, maze.getWidth(), maze.getHeight());

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        gc.fillText(title, maze.getWidth() / 2.0, maze.getHeight() / 2.0 - 10);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        gc.fillText(hint, maze.getWidth() / 2.0, maze.getHeight() / 2.0 + 25);
    }

    public static void showGame(Stage parent) {
        Stage stage = new Stage();
        stage.setTitle("Пакман (Pac-Man)");
        PacmanGamePanel panel = new PacmanGamePanel();
        Scene scene = new Scene(panel, panel.maze.getWidth(), panel.maze.getHeight());
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
