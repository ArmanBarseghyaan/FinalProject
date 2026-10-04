package com.seabattle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Игровое поле 10x10.
 * Отвечает за хранение клеток, валидацию расстановки кораблей с соблюдением
 * 1-клеточной буферной зоны, обработку выстрелов и автоматическое окружение затопленных кораблей.
 */
public class Board {
    public static final int SIZE = 10;
    private final Cell[][] grid;
    private final List<Ship> ships;
    private static final Random RANDOM = new Random();

    public Board() {
        this.grid = new Cell[SIZE][SIZE];
        this.ships = new ArrayList<>();
        initGrid();
    }

    private void initGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell(new Coordinate(r, c));
            }
        }
    }

    public Cell getCell(int row, int col) {
        validateBounds(row, col);
        return grid[row][col];
    }

    public Cell getCell(Coordinate coord) {
        return getCell(coord.getRow(), coord.getCol());
    }

    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    public int getShipCount(ShipType type) {
        int count = 0;
        for (Ship s : ships) {
            if (s.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public int getRemainingCount(ShipType type) {
        return Math.max(0, type.getMaxAllowed() - getShipCount(type));
    }

    /**
     * Проверка корректности размещения корабля.
     * Учитывает лимит кораблей данного типа, границы поля и 1-клеточный буфер со всех сторон (включая диагонали).
     */
    public boolean canPlaceShip(ShipType type, Coordinate start, Orientation orientation) {
        if (type == null || start == null || orientation == null) {
            return false;
        }

        // 0. Проверка лимита флота по типу корабля
        if (getRemainingCount(type) <= 0) {
            return false;
        }

        int size = type.getSize();
        int startRow = start.getRow();
        int startCol = start.getCol();

        // 1. Проверка выхода за границы поля
        if (orientation == Orientation.HORIZONTAL && (startCol + size > SIZE)) {
            return false;
        }
        if (orientation == Orientation.VERTICAL && (startRow + size > SIZE)) {
            return false;
        }

        // 2. Проверка занятости клеток и буферной зоны
        for (int i = 0; i < size; i++) {
            int r = (orientation == Orientation.VERTICAL) ? startRow + i : startRow;
            int c = (orientation == Orientation.HORIZONTAL) ? startCol + i : startCol;

            // Проверяем саму клетку и все 8 соседних вокруг нее
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = r + dr;
                    int nc = c + dc;
                    if (isValidCoordinate(nr, nc)) {
                        if (grid[nr][nc].hasShip()) {
                            return false; // Соседняя клетка или сама клетка уже имеет корабль
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Размещает корабль на поле.
     */
    public boolean placeShip(ShipType type, Coordinate start, Orientation orientation) {
        if (!canPlaceShip(type, start, orientation)) {
            return false;
        }
        Ship ship = new Ship(type, start, orientation);
        for (Coordinate coord : ship.getCoordinates()) {
            grid[coord.getRow()][coord.getCol()].setShip(ship);
        }
        ships.add(ship);
        return true;
    }

    /**
     * Полная очистка поля от кораблей и выстрелов.
     */
    public void clear() {
        ships.clear();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c].clear();
            }
        }
    }

    /**
     * Автоматическая случайная расстановка всех 10 кораблей флота.
     * Полностью очищает поле перед расстановкой.
     */
    public boolean autoPlaceAllShips() {
        clear();
        ShipType[] standardFleet = {
            ShipType.BATTLESHIP,                  // 1x 4-палубный
            ShipType.CRUISER, ShipType.CRUISER,   // 2x 3-палубных
            ShipType.DESTROYER, ShipType.DESTROYER, ShipType.DESTROYER, // 3x 2-палубных
            ShipType.SUBMARINE, ShipType.SUBMARINE, ShipType.SUBMARINE, ShipType.SUBMARINE // 4x 1-палубных
        };

        for (ShipType type : standardFleet) {
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 500) {
                attempts++;
                int r = RANDOM.nextInt(SIZE);
                int c = RANDOM.nextInt(SIZE);
                Orientation orientation = RANDOM.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                Coordinate coord = new Coordinate(r, c);
                if (canPlaceShip(type, coord, orientation)) {
                    placeShip(type, coord, orientation);
                    placed = true;
                }
            }
            if (!placed) {
                // В редком случае тупика — повторяем процесс заново
                return autoPlaceAllShips();
            }
        }
        return true;
    }

    /**
     * Выполнение выстрела по указанной координате.
     */
    public ShotResult shoot(Coordinate coord) {
        Cell cell = getCell(coord);
        CellState currentState = cell.getState();

        if (currentState == CellState.HIT || currentState == CellState.MISS) {
            return ShotResult.ALREADY_SHOT;
        }

        if (cell.hasShip()) {
            cell.setState(CellState.HIT);
            Ship ship = cell.getShip();
            ship.hit();

            if (ship.isSunk()) {
                markSurroundingAsMiss(ship);
                return ShotResult.SUNK;
            }
            return ShotResult.HIT;
        } else {
            cell.setState(CellState.MISS);
            return ShotResult.MISS;
        }
    }

    /**
     * При полном затоплении корабля автоматически помечает все невыбитые клетки вокруг него как MISS.
     */
    private void markSurroundingAsMiss(Ship ship) {
        for (Coordinate coord : ship.getCoordinates()) {
            int r = coord.getRow();
            int c = coord.getCol();
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = r + dr;
                    int nc = c + dc;
                    if (isValidCoordinate(nr, nc)) {
                        Cell neighborCell = grid[nr][nc];
                        if (neighborCell.getState() == CellState.EMPTY) {
                            neighborCell.setState(CellState.MISS);
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверка, затонули ли все корабли флота.
     */
    public boolean allShipsSunk() {
        if (ships.isEmpty()) {
            return false;
        }
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFleetFullyPlaced() {
        for (ShipType type : ShipType.values()) {
            if (getShipCount(type) != type.getMaxAllowed()) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private void validateBounds(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            throw new IllegalArgumentException("Координаты вне поля: row=" + row + ", col=" + col);
        }
    }
}
