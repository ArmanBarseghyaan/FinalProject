package com.seabattle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс корабля, отслеживающий занимаемые клетки и полученные попадания.
 */
public class Ship {
    private final ShipType type;
    private final Orientation orientation;
    private final Coordinate bowCoordinate; // Корма / Носовая начальная координата
    private final List<Coordinate> coordinates;
    private int hitCount;

    public Ship(ShipType type, Coordinate bowCoordinate, Orientation orientation) {
        if (type == null || bowCoordinate == null || orientation == null) {
            throw new IllegalArgumentException("Тип, начальная координата и ориентация не могут быть null");
        }
        this.type = type;
        this.bowCoordinate = bowCoordinate;
        this.orientation = orientation;
        this.hitCount = 0;
        this.coordinates = calculateCoordinates(type.getSize(), bowCoordinate, orientation);
    }

    private static List<Coordinate> calculateCoordinates(int size, Coordinate start, Orientation orientation) {
        List<Coordinate> list = new ArrayList<>();
        int startRow = start.getRow();
        int startCol = start.getCol();

        for (int i = 0; i < size; i++) {
            int r = (orientation == Orientation.VERTICAL) ? startRow + i : startRow;
            int c = (orientation == Orientation.HORIZONTAL) ? startCol + i : startCol;
            if (r >= 10 || c >= 10) {
                throw new IllegalArgumentException("Корабль выходит за границы поля 10x10");
            }
            list.add(new Coordinate(r, c));
        }
        return Collections.unmodifiableList(list);
    }

    public ShipType getType() {
        return type;
    }

    public int getSize() {
        return type.getSize();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Coordinate getBowCoordinate() {
        return bowCoordinate;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public boolean occupies(Coordinate coord) {
        return coordinates.contains(coord);
    }

    /**
     * Фиксация попадания по палубе корабля.
     */
    public boolean hit() {
        if (isSunk()) {
            return false;
        }
        hitCount++;
        return true;
    }

    public int getHitCount() {
        return hitCount;
    }

    /**
     * Затонул ли корабль (все палубы подбиты).
     */
    public boolean isSunk() {
        return hitCount >= type.getSize();
    }
}
