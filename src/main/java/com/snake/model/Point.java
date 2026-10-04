package com.snake.model;

import java.util.Objects;

/**
 * Класс Point представляет координату (x, y) на игровом поле.
 * Демонстрирует использование инкапсуляции, конструкторов, геттеров и сеттеров.
 */
public class Point {
    private int x;
    private int y;

    // Конструктор по умолчанию (0, 0)
    public Point() {
        this(0, 0);
    }

    // Параметризованный конструктор
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Копирующий конструктор
    public Point(Point other) {
        this.x = other.x;
        this.y = other.y;
    }

    // Геттеры и сеттеры
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }
}
