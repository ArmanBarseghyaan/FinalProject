package com.snake.model;

import com.snake.exception.GameOverException;

/**
 * Интерфейс Movable определяет контракт для перемещаемых объектов.
 * Метод move может выбрасывать кастомное исключение GameOverException.
 */
public interface Movable {
    void move() throws GameOverException;
}
