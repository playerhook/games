package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.playerhook.games.util.MapSerializable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Board implements MapSerializable {

    private int firstColumn;
    private int width;
    private int firstRow;
    private int height;

    private final ImmutableTable<Integer, Integer, ImmutableList<TokenPlacement>> board;


    public static Board square(int size, Iterable<TokenPlacement> tokenPlacements) {
        return new Board(0, size, 0, size, tokenPlacements);
    }

    public static Board square(int start, int size, Iterable<TokenPlacement> tokenPlacements) {
        return new Board(start, size, start, size, tokenPlacements);
    }

    public static Board square(int size) {
        return new Board(0, size, 0, size, ImmutableList.of());
    }

    public static Board square(int start, int size) {
        return new Board(start, size, start, size, ImmutableList.of());
    }

    public static Board rectangle(int width, int height, Iterable<TokenPlacement> tokenPlacements) {
        return new Board(0, width, 0, height, tokenPlacements);
    }

    public static Board rectangle(int firstColumn, int width, int firstRow, int height, Iterable<TokenPlacement> tokenPlacements) {
        return new Board(firstColumn, width, firstRow, height, tokenPlacements);
    }

    public static Board rectangle(int width, int height) {
        return new Board(0, width, 0, height, ImmutableList.of());
    }

    public static Board rectangle(int firstColumn, int width, int firstRow, int height) {
        return new Board(firstColumn, width, firstRow, height, ImmutableList.of());
    }

    private Board(int firstColumn, int width, int firstRow, int height, Iterable<TokenPlacement> tokenPlacements) {
        Preconditions.checkArgument(width > 0, "Width must be at least 1");
        Preconditions.checkArgument(height > 0, "Height must be at least 1");
        this.firstColumn = firstColumn;
        this.width = width;
        this.firstRow = firstRow;
        this.height = height;

        Table<Integer, Integer, List<TokenPlacement>> table = HashBasedTable.create();

        for (TokenPlacement placement : tokenPlacements) {
            List<TokenPlacement> tokens = table.get(placement.getDestination().getRow(), placement.getDestination().getColumn());
            if (tokens == null) {
                tokens = Lists.newArrayList();
            }
            tokens.add(placement);
            table.put(placement.getDestination().getRow(), placement.getDestination().getColumn(), tokens);
        }

        ImmutableTable.Builder<Integer, Integer, ImmutableList<TokenPlacement>> tableBuilder = ImmutableTable.builder();

        for (Table.Cell<Integer, Integer, List<TokenPlacement>> cell : table.cellSet()) {
            tableBuilder.put(cell.getRowKey(), cell.getColumnKey(), ImmutableList.copyOf(cell.getValue()));
        }

        this.board = tableBuilder.build();
    }

    public Board place(TokenPlacement placement) {
        return new Board(firstColumn, width, firstRow, height, Iterables.concat(getTokenPlacements(), ImmutableList.of(placement)));
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getWidth() {
        return width;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getHeight() {
        return height;
    }

    public List<TokenPlacement> getTokenPlacements() {
        return ImmutableList.copyOf(Iterables.concat(board.values()));
    }

    public Optional<TokenPlacement> getTokenPlacement(Position position) {
        return Optional.ofNullable(board.get(position.getRow(), position.getColumn())).flatMap(tokenPlacements -> {
            if (tokenPlacements.size() > 0) {
                return Optional.of(tokenPlacements.get(0));
            }
            return Optional.empty();
        });
    }

    public Optional<ImmutableList<TokenPlacement>> getTokenPlacements(Position position) {
        return Optional.ofNullable(board.get(position.getRow(), position.getColumn()));
    }

    public int getLastColumn() {
        return firstColumn + width - 1;
    }

    public int getLastRow() {
        return firstRow + height - 1;
    }

    public boolean contains(Position position) {
        return position.getRow() >= getFirstRow() && position.getRow() <= getLastRow() && position.getColumn() >= getFirstColumn() && position.getColumn() <= getLastColumn();
    }

    public boolean isCompletelyFilled() {
        for (int i = getFirstRow(); i <= getLastRow(); i++) {
            for (int j = getFirstColumn(); j < getLastColumn(); j++) {
                ImmutableList<TokenPlacement> tokens = board.get(i, j);
                if (tokens == null) {
                    return false;
                }

                if (tokens.size() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<String, Object> toMap(PrivacyLevel level) {
        return ImmutableMap.of(
            "firstColumn", firstColumn,
            "width", width,
            "firstRow", firstRow,
            "height", height,
            "tokenPlacements", getTokenPlacements().stream().map((placement) -> placement.toMap(level)).collect(Collectors.toList())
        );
    }

    public static Board load(Object board) {
        if (board == null) {
            return null;
        }
        if (!(board instanceof Map)) {
            throw new IllegalArgumentException("Cannot load board from " + board);
        }
        Map<String, Object> map = (Map<String, Object>) board;

        return new Board(
            MapSerializable.loadInteger(map, "firstColumn"),
            MapSerializable.loadInteger(map, "width"),
            MapSerializable.loadInteger(map, "firstRow"),
            MapSerializable.loadInteger(map, "height"),
            MapSerializable.loadList(map.getOrDefault("tokenPlacements", ImmutableList.of()), TokenPlacement::load)
        );
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board1 = (Board) o;
        return firstColumn == board1.firstColumn &&
                width == board1.width &&
                firstRow == board1.firstRow &&
                height == board1.height &&
                Objects.equal(board, board1.board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(firstColumn, width, firstRow, height, board);
    }
    //CHECKSTYLE:ON
}
