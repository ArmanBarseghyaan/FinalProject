package com.snake.exception;

/**
 * Пользовательское (Custom) исключение.
 * Выбрасывается при столкновении змейки со стеной или сама с собой.
 */
public class GameOverException extends Exception {

    // Конструктор по умолчанию
    public GameOverException() {
        super("Игра окончена! Произошло столкновение.");
    }

    // Конструктор с пользовательским сообщением
    public GameOverException(String message) {
        super(message);
    }
}
