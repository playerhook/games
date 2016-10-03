package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.Map;

public final class Position implements MapSerializable {

    private final int row;
    private final int column;

    public static Position at(int row, int column) {
        return new Position(row, column);
    }

    private Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public Position at(Direction direction) {
        return new Position(row + direction.getRowDelta(), column + direction.getColumnDelta());
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

    @Override
    public Map<String, Object> toMap() {
        return ImmutableMap.of("row", row, "column", column);
    }

    static Position load(Object position) {
        if (position == null) {
            return null;
        }
        if (!(position instanceof Map)) {
            throw new IllegalArgumentException("Cannot load position from " + position);
        }
        Map<String, Object> map = (Map<String, Object>) position;

        return new Position(
            MapSerializable.loadInteger(map, "row"),
            MapSerializable.loadInteger(map, "column")
        );
    }
}
