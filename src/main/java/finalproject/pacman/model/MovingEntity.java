package finalproject.pacman.model;

import javafx.scene.canvas.GraphicsContext;

/**
 * Базовый класс движущихся объектов. Он хранит позицию и реализует общее
 * попиксельное перемещение между центрами соседних клеток лабиринта.
 */
public abstract class MovingEntity {
    private double x;
    private double y;
    private final int startRow;
    private final int startColumn;
    private final double speed;
    private int row;
    private int column;
    private Direction direction = Direction.STOP;

    protected MovingEntity(int startRow, int startColumn, double speed) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.speed = speed;
        resetPosition();
    }

    public final void update(Maze maze, Direction preferredDirection) {
        double centerX = maze.centerX(column);
        double centerY = maze.centerY(row);
        boolean atCenter = Math.abs(x - centerX) < 0.01 && Math.abs(y - centerY) < 0.01;

        if (atCenter) {
            x = centerX;
            y = centerY;
            if (isOpen(maze, row, column, preferredDirection)) {
                direction = preferredDirection;
            } else if (!isOpen(maze, row, column, direction)) {
                direction = Direction.STOP;
            }
        }

        if (direction == Direction.STOP) {
            return;
        }

        GridPoint target = maze.nextCell(row, column, direction);
        if (maze.isTunnelWrap(row, column, direction)) {
            row = target.y();
            column = target.x();
            x = maze.centerX(column);
            y = maze.centerY(row);
            return;
        }
        double targetX = maze.centerX(target.x());
        double targetY = maze.centerY(target.y());
        double distanceToCenter = Math.abs(targetX - x) + Math.abs(targetY - y);

        double distance = Math.min(speed, distanceToCenter);
        x += direction.getColumnOffset() * distance;
        y += direction.getRowOffset() * distance;

        if (distance >= distanceToCenter - 0.001) {
            row = target.y();
            column = target.x();
            x = maze.centerX(column);
            y = maze.centerY(row);
        }
    }

    private boolean isOpen(Maze maze, int row, int column, Direction candidate) {
        if (candidate == Direction.STOP) {
            return false;
        }
        GridPoint next = maze.nextCell(row, column, candidate);
        return maze.isWalkable(next.y(), next.x());
    }

    public final void resetPosition() {
        row = startRow;
        column = startColumn;
        x = startColumn * Maze.TILE_SIZE + Maze.TILE_SIZE / 2.0;
        y = startRow * Maze.TILE_SIZE + Maze.TILE_SIZE / 2.0;
        direction = Direction.STOP;
    }

    public final int getRow(Maze maze) {
        return row;
    }

    public final int getColumn(Maze maze) {
        return column;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final Direction getDirection() {
        return direction;
    }

    public abstract void draw(GraphicsContext gc);
}
