package com.snake.model;

import com.snake.exception.GameOverException;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс Snake представляет саму змейку.
 * Реализует интерфейсы Movable и Drawable.
 */
public class Snake implements Movable, Drawable {
    private final LinkedList<Point> body;
    private Direction direction;
    private Direction nextDirection;
    private boolean growPending;
    private final int gridWidth;
    private final int gridHeight;

    // Конструктор
    public Snake(int initialX, int initialY, int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.body = new LinkedList<>();
        // Начальная длина 3 сегмента
        this.body.add(new Point(initialX, initialY));
        this.body.add(new Point(initialX - 1, initialY));
        this.body.add(new Point(initialX - 2, initialY));

        this.direction = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.growPending = false;
    }

    // Геттеры и сеттеры
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction newDirection) {
        // Запрещаем разворот на 180 градусов
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

        // Проверка столкновения со стенами -> бросаем Exception
        if (nextX < 0 || nextX >= gridWidth || nextY < 0 || nextY >= gridHeight) {
            throw new GameOverException("Столкновение со стеной на позиции (" + nextX + ", " + nextY + ")");
        }

        Point newHead = new Point(nextX, nextY);

        // Проверка самопересечения (столкновения с собственным хвостом) -> бросаем Exception
        for (Point segment : body) {
            if (segment.equals(newHead)) {
                throw new GameOverException("Змейка врезалась в себя!");
            }
        }

        // Добавляем новую голову в начало
        body.addFirst(newHead);

        // Если еда не была съедена, удаляем хвост
        if (!growPending) {
            body.removeLast();
        } else {
            growPending = false; // сбрасываем флаг роста
        }
    }

    @Override
    public void draw(Graphics g, int tileSize) {
        // Отрисовка головы
        g.setColor(new Color(34, 139, 34)); // Темно-зеленый для головы
        Point head = getHead();
        g.fillRect(head.getX() * tileSize, head.getY() * tileSize, tileSize, tileSize);

        // Отрисовка тела
        g.setColor(new Color(50, 205, 50)); // Лаймово-зеленый для тела
        for (int i = 1; i < body.size(); i++) {
            Point p = body.get(i);
            g.fillRect(p.getX() * tileSize, p.getY() * tileSize, tileSize - 1, tileSize - 1);
        }
    }
}
