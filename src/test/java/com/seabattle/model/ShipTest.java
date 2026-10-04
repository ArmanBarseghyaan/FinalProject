package com.seabattle.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты модели корабля (Ship)")
class ShipTest {

    @Test
    @DisplayName("Корректный расчет координат палуб для горизонтального корабля")
    void testCalculateCoordinatesHorizontal() {
        Coordinate start = new Coordinate(2, 3);
        Ship ship = new Ship(ShipType.CRUISER, start, Orientation.HORIZONTAL);

        assertEquals(ShipType.CRUISER, ship.getType());
        assertEquals(3, ship.getSize());
        assertEquals(3, ship.getCoordinates().size());

        assertEquals(new Coordinate(2, 3), ship.getCoordinates().get(0));
        assertEquals(new Coordinate(2, 4), ship.getCoordinates().get(1));
        assertEquals(new Coordinate(2, 5), ship.getCoordinates().get(2));
    }

    @Test
    @DisplayName("Корректный расчет координат палуб для вертикального корабля")
    void testCalculateCoordinatesVertical() {
        Coordinate start = new Coordinate(5, 1);
        Ship ship = new Ship(ShipType.DESTROYER, start, Orientation.VERTICAL);

        assertEquals(2, ship.getSize());
        assertEquals(new Coordinate(5, 1), ship.getCoordinates().get(0));
        assertEquals(new Coordinate(6, 1), ship.getCoordinates().get(1));
    }

    @Test
    @DisplayName("Фиксация попаданий и проверка затопления (isSunk)")
    void testHitsAndSunkState() {
        Ship ship = new Ship(ShipType.DESTROYER, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertFalse(ship.isSunk());
        assertEquals(0, ship.getHitCount());

        assertTrue(ship.hit());
        assertEquals(1, ship.getHitCount());
        assertFalse(ship.isSunk());

        assertTrue(ship.hit());
        assertEquals(2, ship.getHitCount());
        assertTrue(ship.isSunk());

        // Третий выстрел по затонувшему кораблю
        assertFalse(ship.hit());
    }
}
