package com.seabattle.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты игрового поля Морского боя (Board)")
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("Игровое поле должно инициализироваться размером 10x10 и быть пустым")
    void testBoardInitialization() {
        assertEquals(10, Board.SIZE);
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Cell cell = board.getCell(r, c);
                assertNotNull(cell);
                assertEquals(CellState.EMPTY, cell.getState());
                assertFalse(cell.hasShip());
            }
        }
        assertTrue(board.getShips().isEmpty());
    }

    @Test
    @DisplayName("Размещение линкора (4-палубника) на пустом поле")
    void testPlaceBattleship() {
        Coordinate start = new Coordinate(0, 0);
        boolean success = board.placeShip(ShipType.BATTLESHIP, start, Orientation.HORIZONTAL);

        assertTrue(success, "Корабль должен успешно разместиться");
        assertEquals(1, board.getShips().size());
        assertEquals(0, board.getRemainingCount(ShipType.BATTLESHIP));

        for (int c = 0; c < 4; c++) {
            Cell cell = board.getCell(0, c);
            assertTrue(cell.hasShip());
            assertEquals(CellState.SHIP, cell.getState());
        }
    }

    @Test
    @DisplayName("Проверка буферной зоны в 1 клетку вокруг корабля")
    void testBufferZoneConstraint() {
        // Размещаем линкор в (2, 2) по горизонтали: занимает (2,2), (2,3), (2,4), (2,5)
        board.placeShip(ShipType.BATTLESHIP, new Coordinate(2, 2), Orientation.HORIZONTAL);

        // Попытка поставить эсминец слишком близко (в буферную зону 3, 2)
        boolean placedAdjacent = board.placeShip(ShipType.DESTROYER, new Coordinate(3, 2), Orientation.HORIZONTAL);
        assertFalse(placedAdjacent, "Нельзя ставить корабль вплотную или в буферную зону другого корабля");

        // Попытка поставить на расстояние в 2 клетки (в 4, 2) — должно разрешиться
        boolean placedValid = board.placeShip(ShipType.DESTROYER, new Coordinate(4, 2), Orientation.HORIZONTAL);
        assertTrue(placedValid, "Должно быть разрешено разместить с соблюдением буферной зоны");
    }

    @Test
    @DisplayName("Попытка выхода за границы поля 10x10")
    void testOutOfBoundsPlacement() {
        Coordinate start = new Coordinate(0, 8);
        boolean success = board.placeShip(ShipType.BATTLESHIP, start, Orientation.HORIZONTAL);
        assertFalse(success, "4-палубник горизонтально с (0,8) выходит за границы поля");
    }

    @Test
    @DisplayName("Автоматическая расстановка всего флота из 10 кораблей")
    void testAutoPlaceAllShips() {
        boolean autoPlaced = board.autoPlaceAllShips();
        assertTrue(autoPlaced);
        assertEquals(10, board.getShips().size());
        assertTrue(board.isFleetFullyPlaced());
    }

    @Test
    @DisplayName("Выстрел мимо (MISS)")
    void testShootMiss() {
        Coordinate target = new Coordinate(5, 5);
        ShotResult result = board.shoot(target);

        assertEquals(ShotResult.MISS, result);
        assertEquals(CellState.MISS, board.getCell(target).getState());
    }

    @Test
    @DisplayName("Выстрел с попаданием (HIT) и затоплением (SUNK)")
    void testShootHitAndSunk() {
        // Размещаем однопалубник в (1, 1)
        board.placeShip(ShipType.SUBMARINE, new Coordinate(1, 1), Orientation.HORIZONTAL);

        ShotResult result = board.shoot(new Coordinate(1, 1));
        assertEquals(ShotResult.SUNK, result);
        assertEquals(CellState.HIT, board.getCell(1, 1).getState());

        // Вокруг затонувшего корабля ячейки должны автоматически стать MISS
        assertEquals(CellState.MISS, board.getCell(0, 0).getState());
        assertEquals(CellState.MISS, board.getCell(0, 1).getState());
        assertEquals(CellState.MISS, board.getCell(1, 2).getState());
    }

    @Test
    @DisplayName("Повторный выстрел в ту же ячейку возвращает ALREADY_SHOT")
    void testRepeatShot() {
        Coordinate target = new Coordinate(4, 4);
        board.shoot(target);
        ShotResult secondShot = board.shoot(target);

        assertEquals(ShotResult.ALREADY_SHOT, secondShot);
    }
}
