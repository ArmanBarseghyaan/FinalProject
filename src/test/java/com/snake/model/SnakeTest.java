package com.snake.model;

import com.snake.exception.GameOverException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты логики игры «Змейка»")
class SnakeTest {

    @Test
    @DisplayName("Змейка изначально состоит из трех сегментов")
    void startsWithThreeSegments() {
        Snake snake = new Snake(5, 5, 10, 10);

        assertEquals(new Point(5, 5), snake.getHead());
        assertEquals(3, snake.getBody().size());
        assertEquals(Direction.RIGHT, snake.getDirection());
    }

    @Test
    @DisplayName("Змейка перемещается, растет и не разворачивается на 180 градусов")
    void movesGrowsAndRejectsReverseDirection() throws GameOverException {
        Snake snake = new Snake(5, 5, 10, 10);
        snake.setDirection(Direction.LEFT);
        snake.move();
        assertEquals(new Point(6, 5), snake.getHead());
        assertEquals(Direction.RIGHT, snake.getDirection());

        snake.grow();
        snake.move();
        assertEquals(new Point(7, 5), snake.getHead());
        assertEquals(4, snake.getBody().size());

        snake.setDirection(Direction.UP);
        snake.move();
        assertEquals(new Point(7, 4), snake.getHead());
        assertEquals(Direction.UP, snake.getDirection());
    }

    @Test
    @DisplayName("Столкновение со стеной завершает игру")
    void wallCollisionEndsGame() throws GameOverException {
        Snake snake = new Snake(8, 5, 10, 10);
        snake.move();

        GameOverException exception = assertThrows(GameOverException.class, snake::move);
        assertTrue(exception.getMessage().contains("стеной"));
    }

    @Test
    @DisplayName("Еда появляется внутри границ игрового поля")
    void foodSpawnsInsideGrid() {
        Food food = new Food();
        int width = 20;
        int height = 15;

        for (int i = 0; i < 100; i++) {
            food.spawn(width, height);
            assertTrue(food.getPosition().getX() >= 0 && food.getPosition().getX() < width);
            assertTrue(food.getPosition().getY() >= 0 && food.getPosition().getY() < height);
        }
    }
}
