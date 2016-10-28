package org.playerhook.games.api;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Comparator;

public final class Avatar {

    private final int width;
    private final int height;
    private final String buffer;

    public static Avatar of(String buffer) {
        return new Avatar(buffer);
    }

    public static Avatar of(char[][] chars) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            builder.append(chars[i]);
            builder.append('\n');
        }
        builder.replace(builder.length() - 1, builder.length(), "");
        return new Avatar(builder.toString());
    }

    public static Avatar of(Iterable<Iterable<Character>> chars) {
        StringBuilder builder = new StringBuilder();
        for (Iterable<Character> line : chars) {
            for (Character character : line) {
                builder.append(character);
            }
            builder.append('\n');
        }
        builder.replace(builder.length() - 1, builder.length(), "");
        return new Avatar(builder.toString());
    }

    private Avatar(String buffer) {
        this.buffer = buffer.endsWith("\n") ? buffer.substring(0, buffer.lastIndexOf('\n') - 1) : buffer;
        this.width = computeWidth(this.buffer);
        this.height = computeHeight(this.buffer);
    }

    private int computeHeight(String buffer) {
        return buffer.split("\n").length;
    }

    private int computeWidth(String buffer) {
        return Arrays.stream(buffer.split("\n")).max(Comparator.comparingInt(String::length)).map(String::length).orElse(0);
    }

    public ImmutableList<String> getLines() {
        return ImmutableList.copyOf(Arrays.asList(buffer.split("\n")));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return buffer;
    }
}
