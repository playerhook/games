package org.playerhook.games.api;

import com.google.common.base.Objects;

public final class Position {

    private final int row;
    private final int column;

    public static Position of(int row, int column) {
        return new Position(row, column);
    }

    private Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    @Override
    public String toString() {
        return "[r:" + getRow() + ",c:" + getColumn() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position that = (Position) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(row, column);
    }
}
