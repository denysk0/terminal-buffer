package com.example.terminalbuffer;

/**
 * Single character cell in the terminal grid.
 *
 * <p>An empty cell contains a space and {@link Attributes#DEFAULT}.
 */
public record Cell(char character, Attributes attributes) {

    static final Cell EMPTY = new Cell(' ', Attributes.DEFAULT);
}
