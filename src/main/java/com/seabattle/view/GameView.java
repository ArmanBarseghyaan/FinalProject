package com.seabattle.view;

/**
 * Интерфейс представления (View) по паттерну MVC.
 * Позволяет запускать как консольную (CLI), так и графическую (JavaFX GUI) версию игры.
 */
public interface GameView {
    void start();
}
