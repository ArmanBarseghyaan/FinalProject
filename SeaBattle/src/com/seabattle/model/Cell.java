package com.seabattle.model;

/**
 * Класс ячейки игрового поля.
 */
public class Cell {
    private final Coordinate coordinate;
    private CellState state;
    private Ship ship;

    public Cell(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.state = CellState.EMPTY;
        this.ship = null;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        if (state == null) {
            throw new IllegalArgumentException("Состояние ячейки не может быть null.");
        }
        this.state = state;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
        if (ship != null && this.state == CellState.EMPTY) {
            this.state = CellState.SHIP;
        }
    }

    public boolean hasShip() {
        return ship != null;
    }

    public void clear() {
        this.ship = null;
        this.state = CellState.EMPTY;
    }
}
