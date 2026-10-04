package com.seabattle.model;

import java.util.Objects;

/**
 * Неизменяемые координаты на игровом поле (row, col) от 0 до 9,
 * с поддержкой привычной нотации (буква A-J, цифра 1-10).
 */
public final class Coordinate {
    private final int row; // 0..9
    private final int col; // 0..9

    public Coordinate(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            throw new IllegalArgumentException("Координаты должны быть в диапазоне 0..9 (буква A-J, число 1-10). Получено: row=" + row + ", col=" + col);
        }
        this.row = row;
        this.col = col;
    }

    /**
     * Создание из нотации формата "A1", "J10", "c5".
     */
    public static Coordinate parse(String input) {
        if (input == null || input.trim().length() < 2) {
            throw new IllegalArgumentException("Некорректная запись координаты: " + input);
        }
        String clean = input.trim().toUpperCase();
        char colChar = clean.charAt(0);
        if (colChar < 'A' || colChar > 'J') {
            throw new IllegalArgumentException("Некорректная буква столбца (ожидалось A-J): " + colChar);
        }
        int col = colChar - 'A';
        try {
            int rowNumber = Integer.parseInt(clean.substring(1));
            if (rowNumber < 1 || rowNumber > 10) {
                throw new IllegalArgumentException("Номер строки должен быть от 1 до 10: " + rowNumber);
            }
            int row = rowNumber - 1;
            return new Coordinate(row, col);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный номер строки в координате: " + input);
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Буквенное обозначение столбца ('A'..'J').
     */
    public char getColLetter() {
        return (char) ('A' + col);
    }

    /**
     * Отображаемый номер строки (1..10).
     */
    public int getRowNumber() {
        return row + 1;
    }

    /**
     * Форматирует координату в виде "A1", "H8".
     */
    public String toAlgebraic() {
        return "" + getColLetter() + getRowNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
