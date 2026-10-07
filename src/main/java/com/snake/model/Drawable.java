package com.snake.model;

import javafx.scene.canvas.GraphicsContext;

/**
 * Интерфейс Drawable определяет контракт для объектов,
 * которые могут быть отрисованы на графическом контексте JavaFX.
 */
public interface Drawable {
    void draw(GraphicsContext gc, int tileSize);
}
