package com.snake.model;

import java.awt.Graphics;

/**
 * Интерфейс Drawable определяет контракт для объектов,
 * которые могут быть отрисованы на графическом контексте.
 */
public interface Drawable {
    void draw(Graphics g, int tileSize);
}
