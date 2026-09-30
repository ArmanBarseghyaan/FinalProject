package com.snake.model;

import com.snake.exception.GameOverException;

import java.awt.*;
import java.awt.geom.*;
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
    public void draw(Graphics g, int tileSize) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
            g2d.setColor(new Color(0, 0, 0, 70));
            g2d.fillOval(cx + 2, cy + 2, segSize, segSize);

            // 3D Объёмный радиальный градиент (изумрудная шкура змеи)
            Point2D center = new Point2D.Float(cx + segSize * 0.35f, cy + segSize * 0.35f);
            float radius = segSize * 0.75f;
            float[] dist = {0.0f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(85, 225, 65),
                new Color(39, 174, 96),
                new Color(15, 85, 40)
            };
            RadialGradientPaint bodyGrad = new RadialGradientPaint(center, radius, dist, colors);
            g2d.setPaint(bodyGrad);
            g2d.fillOval(cx, cy, segSize, segSize);

            // Чешуйчатый рисунок по центру спины
            g2d.setColor(new Color(10, 65, 30, 150));
            int midX = px + tileSize / 2;
            int midY = py + tileSize / 2;
            g2d.fillOval(midX - 3, midY - 3, 6, 6);

            // Блик на чешуе
            g2d.setColor(new Color(255, 255, 255, 90));
            g2d.fillOval(cx + 3, cy + 3, segSize / 4, segSize / 5);
        }

        // 2. Отрисовка Головы Реалистичной Змейки
        Point head = getHead();
        int hx = head.getX() * tileSize;
        int hy = head.getY() * tileSize;

        // Тень головы
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillOval(hx + 3, hy + 3, tileSize - 2, tileSize - 2);

        // 3D Градиент головы
        Point2D headCenter = new Point2D.Float(hx + tileSize * 0.4f, hy + tileSize * 0.4f);
        float headRadius = tileSize * 0.8f;
        Color[] headColors = {
            new Color(95, 240, 75),
            new Color(42, 185, 85),
            new Color(16, 95, 42)
        };
        g2d.setPaint(new RadialGradientPaint(headCenter, headRadius, new float[]{0f, 0.7f, 1f}, headColors));
        g2d.fillOval(hx + 1, hy + 1, tileSize - 2, tileSize - 2);

        // Раздвоенный красный змеиный язык
        g2d.setColor(new Color(231, 76, 60));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = hx + tileSize / 2;
        int cy = hy + tileSize / 2;

        switch (direction) {
            case RIGHT -> {
                g2d.drawLine(hx + tileSize - 2, cy, hx + tileSize + 6, cy);
                g2d.drawLine(hx + tileSize + 6, cy, hx + tileSize + 9, cy - 3);
                g2d.drawLine(hx + tileSize + 6, cy, hx + tileSize + 9, cy + 3);
                drawSnakeEyes(g2d, hx + tileSize - 9, hy + 5, hx + tileSize - 9, hy + tileSize - 10, direction);
            }
            case LEFT -> {
                g2d.drawLine(hx + 2, cy, hx - 6, cy);
                g2d.drawLine(hx - 6, cy, hx - 9, cy - 3);
                g2d.drawLine(hx - 6, cy, hx - 9, cy + 3);
                drawSnakeEyes(g2d, hx + 4, hy + 5, hx + 4, hy + tileSize - 10, direction);
            }
            case UP -> {
                g2d.drawLine(cx, hy + 2, cx, hy - 6);
                g2d.drawLine(cx, hy - 6, cx - 3, hy - 9);
                g2d.drawLine(cx, hy - 6, cx + 3, hy - 9);
                drawSnakeEyes(g2d, hx + 5, hy + 4, hx + tileSize - 10, hy + 4, direction);
            }
            case DOWN -> {
                g2d.drawLine(cx, hy + tileSize - 2, cx, hy + tileSize + 6);
                g2d.drawLine(cx, hy + tileSize + 6, cx - 3, hy + tileSize + 9);
                g2d.drawLine(cx, hy + tileSize + 6, cx + 3, hy + tileSize + 9);
                drawSnakeEyes(g2d, hx + 5, hy + tileSize - 9, hx + tileSize - 10, hy + tileSize - 9, direction);
            }
        }

        g2d.dispose();
    }

    private void drawSnakeEyes(Graphics2D g2d, int x1, int y1, int x2, int y2, Direction dir) {
        int eyeSize = 6;
        // Золотистый фоновый слой змеиного глаза
        g2d.setColor(new Color(241, 196, 15));
        g2d.fillOval(x1, y1, eyeSize, eyeSize);
        g2d.fillOval(x2, y2, eyeSize, eyeSize);

        // Вертикальный щелевидный зрачок
        g2d.setColor(Color.BLACK);
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            g2d.fillRect(x1 + 2, y1 + 1, 2, 4);
            g2d.fillRect(x2 + 2, y2 + 1, 2, 4);
        } else {
            g2d.fillRect(x1 + 1, y1 + 2, 4, 2);
            g2d.fillRect(x2 + 1, y2 + 2, 4, 2);
        }

        // Блик света на зрачке
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x1 + 1, y1 + 1, 2, 2);
        g2d.fillOval(x2 + 1, y2 + 1, 2, 2);
    }
}
