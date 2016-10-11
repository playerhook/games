package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.Map;

public final class Position implements MapSerializable {

    private final int row;
    private final int column;

    private static final Cache<String, Position> POSITIONS = CacheBuilder.newBuilder().build();

    public static Position at(int row, int column) {
        String key = "" + row + ":" + column;
        Position position = POSITIONS.getIfPresent(key);

        if (position != null) {
            return position;
        }

        position = new Position(row, column);

        POSITIONS.put(key, position);

        return position;
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
    public Map<String, Object> toMap(PrivacyLevel level) {
        return ImmutableMap.of("row", row, "column", column);
    }

    public static Position load(Object position) {
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
