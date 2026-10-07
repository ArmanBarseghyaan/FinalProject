package com.seabattle.view.gui;

import com.seabattle.model.Board;
import com.seabattle.model.Cell;
import com.seabattle.model.CellState;
import com.seabattle.model.Coordinate;
import com.seabattle.model.Orientation;
import com.seabattle.model.Ship;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

/**
 * JavaFX canvas displaying a Sea Battle board and forwarding selected cells.
 */
public final class SeaBattleBoardView extends Canvas {
    private static final double CELL_SIZE = 38;
    private static final double LABEL_SIZE = 26;
    private static final double BOARD_SIZE = LABEL_SIZE + Board.SIZE * CELL_SIZE;

    private final Board board;
    private final boolean fogOfWar;
    private final boolean showWater;
    private final BiConsumer<Integer, Integer> cellAction;

    public SeaBattleBoardView(Board board, boolean fogOfWar, boolean showWater,
                              BiConsumer<Integer, Integer> cellAction) {
        super(BOARD_SIZE, BOARD_SIZE);
        this.board = board;
        this.fogOfWar = fogOfWar;
        this.showWater = showWater;
        this.cellAction = cellAction;
        if (cellAction != null) {
            setCursor(javafx.scene.Cursor.HAND);
        }
        setOnMouseClicked(this::handleClick);
        render();
    }

    private void handleClick(MouseEvent event) {
        if (cellAction == null) {
            return;
        }
        int column = (int) ((event.getX() - LABEL_SIZE) / CELL_SIZE);
        int row = (int) ((event.getY() - LABEL_SIZE) / CELL_SIZE);
        if (row >= 0 && row < Board.SIZE && column >= 0 && column < Board.SIZE) {
            cellAction.accept(row, column);
        }
    }

    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(20, 30, 46));
        gc.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        for (int column = 0; column < Board.SIZE; column++) {
            drawLabel(gc, String.valueOf((char) ('А' + (column >= 9 ? column + 1 : column))),
                    LABEL_SIZE + column * CELL_SIZE, 0, CELL_SIZE, LABEL_SIZE);
        }
        for (int row = 0; row < Board.SIZE; row++) {
            drawLabel(gc, Integer.toString(row + 1), 0, LABEL_SIZE + row * CELL_SIZE,
                    LABEL_SIZE, CELL_SIZE);
            for (int column = 0; column < Board.SIZE; column++) {
                drawCell(gc, board.getCell(row, column), row, column);
            }
        }
    }

    private void drawLabel(GraphicsContext gc, String text, double x, double y, double w, double h) {
        gc.setFill(Color.rgb(22, 30, 42));
        gc.fillRect(x, y, w, h);
        gc.setFill(Color.rgb(170, 185, 205));
        gc.fillText(text, x + 9, y + h - 9);
    }

    private void drawCell(GraphicsContext gc, Cell cell, int row, int column) {
        double x = LABEL_SIZE + column * CELL_SIZE;
        double y = LABEL_SIZE + row * CELL_SIZE;
        CellState state = cell.getState();
        boolean visibleShip = cell.hasShip() && (!fogOfWar || state == CellState.HIT);

        if (showWater || state == CellState.MISS || state == CellState.HIT) {
            gc.setFill(Color.rgb(14, 26, 50));
            gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            long seed = row * 17L + column * 31L;
            gc.setFill(Color.rgb(100, 180, 255, 0.12));
            gc.fillOval(x + 5 + seed % 13, y + 6 + seed % 11, 9, 9);
            gc.setStroke(Color.rgb(60, 130, 210, 0.35));
            gc.strokeArc(x + 3, y + 8 + seed % 14, CELL_SIZE - 6, 7, 0, 180,
                    javafx.scene.shape.ArcType.OPEN);
        } else {
            gc.setFill(Color.rgb(24, 34, 52));
            gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        }

        if (visibleShip) {
            drawShipSegment(gc, cell.getShip(), cell.getCoordinate(), x, y);
        }
        if (state == CellState.HIT) {
            gc.setFill(Color.rgb(255, 155, 20, 0.85));
            gc.fillOval(x + 6, y + 6, CELL_SIZE - 12, CELL_SIZE - 12);
            gc.setStroke(Color.rgb(255, 245, 100));
            gc.strokeLine(x + 12, y + 12, x + CELL_SIZE - 12, y + CELL_SIZE - 12);
            gc.strokeLine(x + CELL_SIZE - 12, y + 12, x + 12, y + CELL_SIZE - 12);
        } else if (state == CellState.MISS) {
            gc.setStroke(Color.rgb(180, 225, 255, 0.85));
            gc.strokeOval(x + 10, y + 10, CELL_SIZE - 20, CELL_SIZE - 20);
            gc.setFill(Color.WHITE);
            gc.fillOval(x + CELL_SIZE / 2 - 2, y + CELL_SIZE / 2 - 2, 4, 4);
        }

        gc.setStroke(Color.rgb(10, 20, 38, 0.75));
        gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
    }

    private void drawShipSegment(GraphicsContext gc, Ship ship, Coordinate coordinate, double x, double y) {
        if (ship == null) {
            return;
        }
        double inset = 5;
        boolean damaged = board.getCell(coordinate).getState() == CellState.HIT;
        gc.setFill(damaged ? Color.rgb(50, 54, 60) : Color.rgb(74, 90, 112));
        gc.fillRoundRect(x + inset, y + inset, CELL_SIZE - 2 * inset,
                CELL_SIZE - 2 * inset, 7, 7);

        int segment = ship.getCoordinates().indexOf(coordinate);
        gc.setFill(damaged ? Color.rgb(35, 38, 44) : Color.rgb(48, 60, 76));
        if (ship.getOrientation() == Orientation.HORIZONTAL) {
            if (segment == 0) {
                gc.fillPolygon(new double[]{x + 2, x + CELL_SIZE - 3, x + CELL_SIZE - 3},
                        new double[]{y + CELL_SIZE / 2, y + 6, y + CELL_SIZE - 6}, 3);
            } else if (segment == ship.getSize() - 1) {
                gc.fillRoundRect(x + 3, y + 6, CELL_SIZE - 6, CELL_SIZE - 12, 8, 8);
            } else {
                gc.fillRect(x + 1, y + 7, CELL_SIZE - 2, CELL_SIZE - 14);
            }
        } else if (segment == 0) {
            gc.fillPolygon(new double[]{x + CELL_SIZE / 2, x + 6, x + CELL_SIZE - 6},
                    new double[]{y + 2, y + CELL_SIZE - 3, y + CELL_SIZE - 3}, 3);
        } else if (segment == ship.getSize() - 1) {
            gc.fillRoundRect(x + 6, y + 3, CELL_SIZE - 12, CELL_SIZE - 6, 8, 8);
        } else {
            gc.fillRect(x + 7, y + 1, CELL_SIZE - 14, CELL_SIZE - 2);
        }
        gc.setFill(damaged ? Color.GRAY : Color.rgb(40, 50, 65));
        gc.fillOval(x + CELL_SIZE / 2 - 4, y + CELL_SIZE / 2 - 4, 8, 8);
    }
}
