package com.snake.model;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 * Класс Food представляет еду для змейки.
 * Реализует интерфейс Drawable.
 */
public class Food implements Drawable {
    private Point position;
    private final Random random;

    // Конструктор
    public Food() {
        this.position = new Point(0, 0);
        this.random = new Random();
    }

    // Геттеры и сеттеры
    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Генерирует новую случайную позицию для еды на сетке.
     */
    public void spawn(int gridWidth, int gridHeight) {
        int x = random.nextInt(gridWidth);
        int y = random.nextInt(gridHeight);
        this.position.setX(x);
        this.position.setY(y);
    }

    @Override
    public void draw(Graphics g, int tileSize) {
        g.setColor(Color.RED);
        g.fillOval(position.getX() * tileSize, position.getY() * tileSize, tileSize, tileSize);
    }
}
