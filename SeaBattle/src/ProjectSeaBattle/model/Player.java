package com.seabattle.model;

/**
 * Модель Игрока. Хранит имя игрока и его собственное игровое поле.
 */
public class Player {
    private final String name;
    private final Board board;
    private boolean ready;

    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя игрока не может быть пустым");
        }
        this.name = name;
        this.board = new Board();
        this.ready = false;
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void reset() {
        board.clear();
        ready = false;
    }

    @Override
    public String toString() {
        return name;
    }
}
