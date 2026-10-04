package com.seabattle.model;

/**
 * Фазы игрового процесса (паттерн State / Состояния).
 */
public enum GamePhase {
    PLACEMENT_PLAYER_1("Расстановка кораблей: Игрок 1"),
    PLACEMENT_PLAYER_2("Расстановка кораблей: Игрок 2"),
    PLAYER_1_TURN("Ход Игрока 1"),
    PLAYER_2_TURN("Ход Игрока 2"),
    SWITCHING_PLAYER("Передача хода сопернику"),
    GAME_OVER("Игра окончена");

    private final String description;

    GamePhase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
