package finalproject.pacman.model;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Симметричный лабиринт в классическом стиле Pac-Man: боковой туннель,
 * центральный домик призраков и несколько типов точек.
 */
public final class Maze {
    public static final int TILE_SIZE = 28;
    public static final int COLUMNS = 28;
    public static final int ROWS = 31;
    public static final int TUNNEL_ROW = 15;
    private static final int HOUSE_TOP = 13;
    private static final int HOUSE_BOTTOM = 17;
    private static final int HOUSE_LEFT = 10;
    private static final int HOUSE_RIGHT = 17;

    private final boolean[][] walls = new boolean[ROWS][COLUMNS];
    private final boolean[][] pellets = new boolean[ROWS][COLUMNS];
    private final boolean[][] energizers = new boolean[ROWS][COLUMNS];
    private int remainingPellets;

    public Maze() {
        createMap();
        validatePassages();
        resetCollectibles();
    }

    private void createMap() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                walls[row][column] = row == 0 || row == ROWS - 1
                        || column == 0 || column == COLUMNS - 1;
            }
        }

        // Парные прямоугольные стены образуют характерные синие коридоры.
        int[] blockRows = {3, 5, 8, 10, 19, 21, 24, 26};
        int[] blockColumns = {3, 8, 18, 23};
        for (int row : blockRows) {
            for (int column : blockColumns) {
                for (int rowOffset = 0; rowOffset < 2; rowOffset++) {
                    for (int columnOffset = 0; columnOffset < 2; columnOffset++) {
                        walls[row + rowOffset][column + columnOffset] = true;
                    }
                }
            }
        }

        // Открываем крайние клетки симметричного горизонтального туннеля.
        walls[TUNNEL_ROW][0] = false;
        walls[TUNNEL_ROW][COLUMNS - 1] = false;

        // Контур центрального домика с двумя дверными клетками сверху.
        for (int column = HOUSE_LEFT; column <= HOUSE_RIGHT; column++) {
            if (column != 13 && column != 14) {
                walls[HOUSE_TOP][column] = true;
            }
            walls[HOUSE_BOTTOM][column] = true;
        }
        for (int row = HOUSE_TOP + 1; row < HOUSE_BOTTOM; row++) {
            walls[row][HOUSE_LEFT] = true;
            walls[row][HOUSE_RIGHT] = true;
        }
    }

    private void validatePassages() {
        boolean[][] visited = new boolean[ROWS][COLUMNS];
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(new Point(1, 1));
        visited[1][1] = true;

        while (!queue.isEmpty()) {
            Point cell = queue.remove();
            for (Direction direction : cardinalDirections()) {
                Point next = nextCell(cell.y, cell.x, direction);
                if (isWalkable(next.y, next.x) && !visited[next.y][next.x]) {
                    visited[next.y][next.x] = true;
                    queue.add(next);
                }
            }
        }

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (!isWalkable(row, column)) {
                    continue;
                }
                if (!visited[row][column]) {
                    throw new IllegalStateException("В лабиринте обнаружен изолированный проход.");
                }

                int exits = 0;
                for (Direction direction : cardinalDirections()) {
                    Point next = nextCell(row, column, direction);
                    if (isWalkable(next.y, next.x)) {
                        exits++;
                    }
                }
                if (exits < 2) {
                    throw new IllegalStateException("В лабиринте обнаружен тупик.");
                }
            }
        }
    }

    public void resetCollectibles() {
        remainingPellets = 0;
        for (int row = 0; row < ROWS; row++) {
            Arrays.fill(pellets[row], false);
            Arrays.fill(energizers[row], false);
            for (int column = 0; column < COLUMNS; column++) {
                if (isWalkable(row, column) && !isInsideGhostHouse(row, column)) {
                    pellets[row][column] = true;
                    remainingPellets++;
                }
            }
        }

        clearPellet(23, 13);
        clearPellet(TUNNEL_ROW, 0);
        clearPellet(TUNNEL_ROW, COLUMNS - 1);
        placeEnergizer(3, 1);
        placeEnergizer(3, COLUMNS - 2);
        placeEnergizer(ROWS - 4, 1);
        placeEnergizer(ROWS - 4, COLUMNS - 2);
    }

    private void placeEnergizer(int row, int column) {
        clearPellet(row, column);
        energizers[row][column] = true;
        remainingPellets++;
    }

    private void clearPellet(int row, int column) {
        if (pellets[row][column]) {
            pellets[row][column] = false;
            remainingPellets--;
        }
    }

    public boolean isWalkable(int row, int column) {
        return row >= 0 && row < ROWS && column >= 0 && column < COLUMNS
                && !walls[row][column];
    }

    public boolean isInsideGhostHouse(int row, int column) {
        return row > HOUSE_TOP && row < HOUSE_BOTTOM
                && column > HOUSE_LEFT && column < HOUSE_RIGHT;
    }

    public boolean isGhostDoor(int row, int column) {
        return row == HOUSE_TOP && (column == 13 || column == 14);
    }

    public boolean hasPellet(int row, int column) {
        return pellets[row][column];
    }

    public boolean hasEnergizer(int row, int column) {
        return energizers[row][column];
    }

    public boolean collectPellet(int row, int column) {
        if (!pellets[row][column]) {
            return false;
        }
        pellets[row][column] = false;
        remainingPellets--;
        return true;
    }

    public boolean collectEnergizer(int row, int column) {
        if (!energizers[row][column]) {
            return false;
        }
        energizers[row][column] = false;
        remainingPellets--;
        return true;
    }

    public boolean isTunnelWrap(int row, int column, Direction direction) {
        return row == TUNNEL_ROW
                && ((column == 0 && direction == Direction.LEFT)
                || (column == COLUMNS - 1 && direction == Direction.RIGHT));
    }

    public int getRemainingPellets() {
        return remainingPellets;
    }

    public int getWidth() {
        return COLUMNS * TILE_SIZE;
    }

    public int getHeight() {
        return ROWS * TILE_SIZE;
    }

    public double centerX(int column) {
        return column * TILE_SIZE + TILE_SIZE / 2.0;
    }

    public double centerY(int row) {
        return row * TILE_SIZE + TILE_SIZE / 2.0;
    }

    public Point nextCell(int row, int column, Direction direction) {
        int nextRow = row + direction.getRowOffset();
        int nextColumn = column + direction.getColumnOffset();
        if (row == TUNNEL_ROW && nextColumn < 0) {
            nextColumn = COLUMNS - 1;
        } else if (row == TUNNEL_ROW && nextColumn >= COLUMNS) {
            nextColumn = 0;
        }
        return new Point(nextColumn, nextRow);
    }

    /**
     * Выбирает кратчайший маршрут к цели с учётом перехода через туннель.
     */
    public Direction directionToward(int fromRow, int fromColumn,
                                     int targetRow, int targetColumn,
                                     Direction currentDirection) {
        return directionByDistance(fromRow, fromColumn, targetRow, targetColumn,
                currentDirection, false);
    }

    /**
     * Выбирает маршрут, который увеличивает расстояние до Пакмана.
     */
    public Direction directionAway(int fromRow, int fromColumn,
                                   int targetRow, int targetColumn,
                                   Direction currentDirection) {
        return directionByDistance(fromRow, fromColumn, targetRow, targetColumn,
                currentDirection, true);
    }

    private Direction directionByDistance(int fromRow, int fromColumn,
                                          int targetRow, int targetColumn,
                                          Direction currentDirection, boolean flee) {
        int[][] distances = new int[ROWS][COLUMNS];
        for (int[] row : distances) {
            Arrays.fill(row, -1);
        }

        Queue<Point> queue = new ArrayDeque<>();
        queue.add(new Point(targetColumn, targetRow));
        distances[targetRow][targetColumn] = 0;

        while (!queue.isEmpty()) {
            Point cell = queue.remove();
            for (Direction direction : cardinalDirections()) {
                Point next = nextCell(cell.y, cell.x, direction);
                if (isWalkable(next.y, next.x) && distances[next.y][next.x] < 0) {
                    distances[next.y][next.x] = distances[cell.y][cell.x] + 1;
                    queue.add(next);
                }
            }
        }

        Direction bestDirection = Direction.STOP;
        int bestDistance = flee ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int bestReversePenalty = Integer.MAX_VALUE;
        for (Direction direction : cardinalDirections()) {
            Point next = nextCell(fromRow, fromColumn, direction);
            if (!isWalkable(next.y, next.x) || distances[next.y][next.x] < 0) {
                continue;
            }

            int distance = distances[next.y][next.x];
            int reversePenalty = direction == currentDirection.opposite() ? 1 : 0;
            boolean betterDistance = flee ? distance > bestDistance : distance < bestDistance;
            if (betterDistance || (distance == bestDistance && reversePenalty < bestReversePenalty)) {
                bestDirection = direction;
                bestDistance = distance;
                bestReversePenalty = reversePenalty;
            }
        }
        return bestDirection;
    }

    private static Direction[] cardinalDirections() {
        return new Direction[]{Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
    }
}
