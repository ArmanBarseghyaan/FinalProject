package com.seabattle.view;

/**
 * Интерфейс представления (View) по паттерну MVC.
 * Позволяет запускать как консольную (CLI), так и графическую (Swing GUI) версию игры.
 */
public interface GameView {
    void start();
}
