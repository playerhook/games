package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;

import java.util.List;
import java.util.Optional;

public final class Board {

    private int firstColumn;
    private int width;
    private int firstRow;
    private int height;

    private final ImmutableTable<Integer, Integer, TokenPlacement> board;


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
        this.firstColumn = firstColumn;
        this.width = width;
        this.firstRow = firstRow;
        this.height = height;

        ImmutableTable.Builder<Integer, Integer, TokenPlacement> tableBuilder = ImmutableTable.builder();

        for (TokenPlacement placement : tokenPlacements) {
            tableBuilder.put(placement.getPosition().getRow(), placement.getPosition().getColumn(), placement);
        }

        this.board = tableBuilder.build();
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
        return ImmutableList.copyOf(board.values());
    }

    public Optional<TokenPlacement> getTokenPlacement(Position position) {
        return Optional.ofNullable(board.get(position.getRow(), position.getColumn()));
    }

    public int getLastColumn() {
        return firstColumn + width - 1;
    }

    public int getLastRow() {
        return firstRow + height - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Board board1 = (Board) o;
        return firstColumn == board1.firstColumn && width == board1.width && firstRow == board1.firstRow && height == board1.height && Objects.equal(board, board1.board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(firstColumn, width, firstRow, height, board);
    }
}
