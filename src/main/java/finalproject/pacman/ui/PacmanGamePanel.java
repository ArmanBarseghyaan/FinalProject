package finalproject.pacman.ui;

import finalproject.pacman.model.Direction;
import finalproject.pacman.model.Ghost;
import finalproject.pacman.model.Maze;
import finalproject.pacman.model.Pacman;
import finalproject.pacman.audio.SoundManager;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Игровая Swing-панель. Таймер обновляет состояние примерно 60 раз в секунду,
 * а привязки клавиш работают независимо от фокуса на дочерних элементах.
 */
public final class PacmanGamePanel extends JPanel {
    private static final int FRAME_DELAY_MS = 16;
    private static final long FRIGHTENED_DURATION_NANOS = 7_000_000_000L;
    private static final int GHOST_HOUSE_ROW = 15;
    private static final Color BACKGROUND = new Color(10, 13, 31);
    private static final Color WALL_COLOR = new Color(27, 71, 166);

    private final Maze maze = new Maze();
    private final Pacman pacman = new Pacman(1, 1);
    private final List<Ghost> ghosts = new ArrayList<>();
    private final SoundManager sounds = new SoundManager();
    private final Timer timer;
    private int score;
    private int lives = 3;
    private boolean gameOver;
    private boolean won;
    private boolean paused;
    private boolean started;
    private long frightenedUntil;
    private int ghostsEaten;

    public PacmanGamePanel() {
        setPreferredSize(new Dimension(maze.getWidth(), maze.getHeight()));
        setBackground(BACKGROUND);
        setFocusable(true);
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 12, new Color(245, 73, 96)));
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 13, new Color(70, 222, 235)));
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 14, new Color(255, 154, 65)));
        ghosts.add(new Ghost(GHOST_HOUSE_ROW, 15, new Color(220, 105, 235)));

        installKeyBindings();
        timer = new Timer(FRAME_DELAY_MS, event -> updateGame());
        timer.start();
    }

    private void installKeyBindings() {
        bindDirection("left", Direction.LEFT, key(KeyEvent.VK_LEFT), key(KeyEvent.VK_A));
        bindDirection("right", Direction.RIGHT, key(KeyEvent.VK_RIGHT), key(KeyEvent.VK_D));
        bindDirection("up", Direction.UP, key(KeyEvent.VK_UP), key(KeyEvent.VK_W));
        bindDirection("down", Direction.DOWN, key(KeyEvent.VK_DOWN), key(KeyEvent.VK_S));
        bindAction("pause", key(KeyEvent.VK_P), event -> {
            if (started && !gameOver) {
                paused = !paused;
                if (paused) {
                    sounds.pause();
                } else {
                    sounds.resume();
                }
                repaint();
            }
        });
        bindAction("start-enter", key(KeyEvent.VK_ENTER), event -> startOrRestartGame());
        bindAction("start-space", key(KeyEvent.VK_SPACE), event -> startOrRestartGame());
    }

    private KeyStroke key(int keyCode) {
        return KeyStroke.getKeyStroke(keyCode, 0);
    }

    private void bindDirection(String name, Direction direction, KeyStroke... strokes) {
        bindAction(name, strokes, event -> {
            if (!gameOver) {
                pacman.requestDirection(direction);
            }
        });
    }

    private void bindAction(String name, KeyStroke stroke, GameAction action) {
        bindAction(name, new KeyStroke[]{stroke}, action);
    }

    private void bindAction(String name, KeyStroke[] strokes, GameAction action) {
        for (KeyStroke stroke : strokes) {
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(stroke, name);
        }
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.perform(event);
            }
        });
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
            timer.stop();
            sounds.stopAll();
            repaint();
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
        repaint();
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
                    timer.stop();
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
        repaint();
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void startOrRestartGame() {
        if (!started || gameOver || won) {
            restartGame();
        }
    }

    public void stopGame() {
        timer.stop();
        sounds.close();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawMaze(g);
        drawStatus(g);
        pacman.draw(g);
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }

        if (!started) {
            drawOverlay(g, "ПАКМАН", "Нажмите Enter или пробел, чтобы начать");
        } else if (paused || gameOver) {
            drawOverlay(g, paused ? "ПАУЗА" : (won ? "ПОБЕДА!" : "ИГРА ОКОНЧЕНА"),
                    paused ? "Нажмите P, чтобы продолжить"
                            : "Нажмите Enter или пробел, чтобы начать заново");
        }
        g.dispose();
    }

    private void drawMaze(Graphics2D g) {
        for (int row = 0; row < Maze.ROWS; row++) {
            for (int column = 0; column < Maze.COLUMNS; column++) {
                int x = column * Maze.TILE_SIZE;
                int y = row * Maze.TILE_SIZE;
                if (!maze.isWalkable(row, column)) {
                    g.setColor(WALL_COLOR);
                    g.fillRoundRect(x + 2, y + 2, Maze.TILE_SIZE - 4,
                            Maze.TILE_SIZE - 4, 8, 8);
                    g.setColor(new Color(54, 116, 225));
                    g.setStroke(new BasicStroke(1.2f));
                    g.drawRoundRect(x + 4, y + 4, Maze.TILE_SIZE - 8,
                            Maze.TILE_SIZE - 8, 6, 6);
                } else if (maze.hasEnergizer(row, column)) {
                    g.setColor(new Color(255, 236, 190));
                    int size = 14;
                    int inset = (Maze.TILE_SIZE - size) / 2;
                    g.fillOval(x + inset, y + inset, size, size);
                } else if (maze.hasPellet(row, column)) {
                    g.setColor(new Color(255, 224, 170));
                    int dotSize = 5;
                    g.fillOval(x + (Maze.TILE_SIZE - dotSize) / 2,
                            y + (Maze.TILE_SIZE - dotSize) / 2, dotSize, dotSize);
                }
                if (maze.isGhostDoor(row, column)) {
                    g.setColor(new Color(255, 170, 205));
                    g.fillRect(x + 2, y + Maze.TILE_SIZE / 2 - 2,
                            Maze.TILE_SIZE - 4, 4);
                }
            }
        }
    }

    private void drawStatus(Graphics2D g) {
        g.setColor(new Color(10, 13, 31, 210));
        g.fillRect(0, 0, maze.getWidth(), 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString("Счёт: " + score + "     Жизни: " + lives
                + "     Точки: " + maze.getRemainingPellets()
                + "     Стрелки/WASD — движение   P — пауза",
                10, 20);
    }

    private void drawOverlay(Graphics2D g, String title, String hint) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, 0, maze.getWidth(), maze.getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        FontMetrics titleMetrics = g.getFontMetrics();
        g.drawString(title, (maze.getWidth() - titleMetrics.stringWidth(title)) / 2,
                maze.getHeight() / 2 - 10);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        FontMetrics hintMetrics = g.getFontMetrics();
        g.drawString(hint, (maze.getWidth() - hintMetrics.stringWidth(hint)) / 2,
                maze.getHeight() / 2 + 25);
    }

    @FunctionalInterface
    private interface GameAction {
        void perform(ActionEvent event);
    }
}
