package com.snake.model;

/**
 * Перечисление направлений движения змейки.
 */
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    /**
     * Проверяет, является ли направление противоположным данному.
     */
    public boolean isOpposite(Direction other) {
        if (other == null) return false;
        return (this == UP && other == DOWN) ||
               (this == DOWN && other == UP) ||
               (this == LEFT && other == RIGHT) ||
               (this == RIGHT && other == LEFT);
    }
}
