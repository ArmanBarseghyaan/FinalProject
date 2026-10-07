package com.snake.model;

import com.snake.exception.GameOverException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.LinkedList;
import java.util.List;

/**
 * Класс Snake представляет реалистичную 3D-змейку с чешуей, хищными глазами, раздвоенным языком и объемными сегментами.
 * Реализует интерфейсы Movable и Drawable.
 */
public class Snake implements Movable, Drawable {
    private final LinkedList<Point> body;
    private Direction direction;
    private Direction nextDirection;
    private boolean growPending;
    private final int gridWidth;
    private final int gridHeight;

    public Snake(int initialX, int initialY, int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.body = new LinkedList<>();
        this.body.add(new Point(initialX, initialY));
        this.body.add(new Point(initialX - 1, initialY));
        this.body.add(new Point(initialX - 2, initialY));

        this.direction = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.growPending = false;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction newDirection) {
        if (newDirection != null && !this.direction.isOpposite(newDirection)) {
            this.nextDirection = newDirection;
        }
    }

    public Point getHead() {
        return body.getFirst();
    }

    public List<Point> getBody() {
        return body;
    }

    public void grow() {
        this.growPending = true;
    }

    @Override
    public void move() throws GameOverException {
        this.direction = nextDirection;
        Point currentHead = getHead();

        int nextX = currentHead.getX();
        int nextY = currentHead.getY();

        switch (direction) {
            case UP -> nextY--;
            case DOWN -> nextY++;
            case LEFT -> nextX--;
            case RIGHT -> nextX++;
        }

        if (nextX < 0 || nextX >= gridWidth || nextY < 0 || nextY >= gridHeight) {
            throw new GameOverException("Столкновение со стеной на позиции (" + nextX + ", " + nextY + ")");
        }

        Point newHead = new Point(nextX, nextY);

        for (Point segment : body) {
            if (segment.equals(newHead)) {
                throw new GameOverException("Змейка врезалась в себя!");
            }
        }

        body.addFirst(newHead);

        if (!growPending) {
            body.removeLast();
        } else {
            growPending = false;
        }
    }

    @Override
    public void draw(GraphicsContext gc, int tileSize) {
        int totalSegments = body.size();

        // 1. Отрисовка тела змейки (от хвоста к шее)
        for (int i = totalSegments - 1; i >= 1; i--) {
            Point p = body.get(i);
            int px = p.getX() * tileSize;
            int py = p.getY() * tileSize;

            boolean isTail = (i == totalSegments - 1);
            float scaleFactor = isTail ? 0.72f : 0.90f;
            int segSize = (int) (tileSize * scaleFactor);
            int offset = (tileSize - segSize) / 2;

            int cx = px + offset;
            int cy = py + offset;

            // Тень сегмента
            gc.setFill(Color.rgb(0, 0, 0, 70 / 255.0));
            gc.fillOval(cx + 2, cy + 2, segSize, segSize);

            // 3D Объёмный радиальный градиент (изумрудная шкура змеи)
            RadialGradient bodyGrad = new RadialGradient(
                    0, 0,
                    cx + segSize * 0.35, cy + segSize * 0.35,
                    segSize * 0.75,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(85, 225, 65)),
                    new Stop(0.7, Color.rgb(39, 174, 96)),
                    new Stop(1.0, Color.rgb(15, 85, 40))
            );
            gc.setFill(bodyGrad);
            gc.fillOval(cx, cy, segSize, segSize);

            // Чешуйчатый рисунок по центру спины
            gc.setFill(Color.rgb(10, 65, 30, 150 / 255.0));
            int midX = px + tileSize / 2;
            int midY = py + tileSize / 2;
            gc.fillOval(midX - 3, midY - 3, 6, 6);

            // Блик на чешуе
            gc.setFill(Color.rgb(255, 255, 255, 90 / 255.0));
            gc.fillOval(cx + 3, cy + 3, segSize / 4.0, segSize / 5.0);
        }

        // 2. Отрисовка Головы Реалистичной Змейки
        Point head = getHead();
        int hx = head.getX() * tileSize;
        int hy = head.getY() * tileSize;

        // Тень головы
        gc.setFill(Color.rgb(0, 0, 0, 90 / 255.0));
        gc.fillOval(hx + 3, hy + 3, tileSize - 2, tileSize - 2);

        // 3D Градиент головы
        RadialGradient headGrad = new RadialGradient(
                0, 0,
                hx + tileSize * 0.4, hy + tileSize * 0.4,
                tileSize * 0.8,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(95, 240, 75)),
                new Stop(0.7, Color.rgb(42, 185, 85)),
                new Stop(1.0, Color.rgb(16, 95, 42))
        );
        gc.setFill(headGrad);
        gc.fillOval(hx + 1, hy + 1, tileSize - 2, tileSize - 2);

        // Раздвоенный красный змеиный язык
        gc.setStroke(Color.rgb(231, 76, 60));
        gc.setLineWidth(2.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        int cx = hx + tileSize / 2;
        int cy = hy + tileSize / 2;

        switch (direction) {
            case RIGHT -> {
                gc.strokeLine(hx + tileSize - 2, cy, hx + tileSize + 6, cy);
                gc.strokeLine(hx + tileSize + 6, cy, hx + tileSize + 9, cy - 3);
                gc.strokeLine(hx + tileSize + 6, cy, hx + tileSize + 9, cy + 3);
                drawSnakeEyes(gc, hx + tileSize - 9, hy + 5, hx + tileSize - 9, hy + tileSize - 10, direction);
            }
            case LEFT -> {
                gc.strokeLine(hx + 2, cy, hx - 6, cy);
                gc.strokeLine(hx - 6, cy, hx - 9, cy - 3);
                gc.strokeLine(hx - 6, cy, hx - 9, cy + 3);
                drawSnakeEyes(gc, hx + 4, hy + 5, hx + 4, hy + tileSize - 10, direction);
            }
            case UP -> {
                gc.strokeLine(cx, hy + 2, cx, hy - 6);
                gc.strokeLine(cx, hy - 6, cx - 3, hy - 9);
                gc.strokeLine(cx, hy - 6, cx + 3, hy - 9);
                drawSnakeEyes(gc, hx + 5, hy + 4, hx + tileSize - 10, hy + 4, direction);
            }
            case DOWN -> {
                gc.strokeLine(cx, hy + tileSize - 2, cx, hy + tileSize + 6);
                gc.strokeLine(cx, hy + tileSize + 6, cx - 3, hy + tileSize + 9);
                gc.strokeLine(cx, hy + tileSize + 6, cx + 3, hy + tileSize + 9);
                drawSnakeEyes(gc, hx + 5, hy + tileSize - 9, hx + tileSize - 10, hy + tileSize - 9, direction);
            }
        }
    }

    private void drawSnakeEyes(GraphicsContext gc, int x1, int y1, int x2, int y2, Direction dir) {
        int eyeSize = 6;
        // Золотистый фоновый слой змеиного глаза
        gc.setFill(Color.rgb(241, 196, 15));
        gc.fillOval(x1, y1, eyeSize, eyeSize);
        gc.fillOval(x2, y2, eyeSize, eyeSize);

        // Вертикальный щелевидный зрачок
        gc.setFill(Color.BLACK);
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            gc.fillRect(x1 + 2, y1 + 1, 2, 4);
            gc.fillRect(x2 + 2, y2 + 1, 2, 4);
        } else {
            gc.fillRect(x1 + 1, y1 + 2, 4, 2);
            gc.fillRect(x2 + 1, y2 + 2, 4, 2);
        }

        // Блик света на зрачке
        gc.setFill(Color.WHITE);
        gc.fillOval(x1 + 1, y1 + 1, 2, 2);
        gc.fillOval(x2 + 1, y2 + 1, 2, 2);
    }
}
