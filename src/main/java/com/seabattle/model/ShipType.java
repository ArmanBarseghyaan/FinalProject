package com.seabattle.model;

/**
 * Перечисление типов кораблей классического Морского боя.
 */
public enum ShipType {
    BATTLESHIP(4, "4-палубный Линкор"),
    CRUISER(3, "3-палубный Крейсер"),
    DESTROYER(2, "2-палубный Эсминец"),
    SUBMARINE(1, "1-палубный Торпедный катер");

    private final int size;
    private final String name;

    ShipType(int size, String name) {
        this.size = size;
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public int getMaxAllowed() {
        switch (this) {
            case BATTLESHIP: return 1;
            case CRUISER: return 2;
            case DESTROYER: return 3;
            case SUBMARINE: return 4;
            default: return 0;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
