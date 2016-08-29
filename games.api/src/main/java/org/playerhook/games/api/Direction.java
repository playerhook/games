package org.playerhook.games.api;

public enum Direction {

    UPPER_LEFT(-1, -1),
    UP(-1, 0),
    UPPER_RIGHT(-1, 1),
    LEFT(0, -1),
    RIGHT(0, 1),
    LOWER_LEFT(1, -1),
    DOWN(1, 0),
    LOWER_RIGHT(1, 1);

    private final int rowDelta;
    private final int columnDelta;

    Direction(int rowDelta, int columnDelta) {
        this.rowDelta = rowDelta;
        this.columnDelta = columnDelta;
    }

    public int getRowDelta() {
        return rowDelta;
    }

    public int getColumnDelta() {
        return columnDelta;
    }
}
