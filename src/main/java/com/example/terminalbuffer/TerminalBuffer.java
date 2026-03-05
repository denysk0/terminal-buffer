package com.example.terminalbuffer;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;

/**
 * A terminal text buffer consisting of a screen and a scrollback history.
 *
 * <p>The screen is a grid of {@code width * height} {@link Cell}s.
 * Lines scrolled off the top are moved into the scrollback,
 * capped at {@code scrollbackMax} (oldest lines are removed if the cap is reached).
 */
public class TerminalBuffer {

    private final int width;
    private final int height;
    private final int scrollbackMax;

    private final Deque<Cell[]> screen;
    private final Deque<Cell[]> scrollback;

    private int cursorCol = 0;
    private int cursorRow = 0;
    private Attributes currentAttributes = Attributes.DEFAULT;

    public TerminalBuffer(int width, int height, int scrollbackMax) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive, got: " + height);
        }
        if (scrollbackMax < 0) {
            throw new IllegalArgumentException(
                    "scrollbackMax must be non-negative, got: " + scrollbackMax);
        }

        this.width = width;
        this.height = height;
        this.scrollbackMax = scrollbackMax;

        this.screen = new ArrayDeque<>(height);
        this.scrollback = new ArrayDeque<>();

        for (int i = 0; i < height; i++) {
            screen.addLast(emptyRow());
        }
    }

    // ---------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getScrollbackMax() { return scrollbackMax; }
    public int getScrollbackSize() { return scrollback.size(); }

    // ---------------------------------------------------------------
    // Cursor
    // ---------------------------------------------------------------

    public CursorPosition getCursorPosition() {
        return new CursorPosition(cursorCol, cursorRow);
    }

    public int getCursorCol() { return cursorCol; }
    public int getCursorRow() { return cursorRow; }

    public void setCursorPosition(int col, int row) {
        cursorCol = clampCol(col);
        cursorRow = clampRow(row);
    }

    public void moveRight(int n) { checkNonNegative(n); cursorCol = clampCol(cursorCol + n); }
    public void moveLeft(int n)  { checkNonNegative(n); cursorCol = clampCol(cursorCol - n); }
    public void moveDown(int n)  { checkNonNegative(n); cursorRow = clampRow(cursorRow + n); }
    public void moveUp(int n)    { checkNonNegative(n); cursorRow = clampRow(cursorRow - n); }

    // ---------------------------------------------------------------
    // Current attributes
    // ---------------------------------------------------------------

    public Attributes getCurrentAttributes() { return currentAttributes; }

    public void setCurrentAttributes(Attributes attributes) {
        currentAttributes = (attributes == null) ? Attributes.DEFAULT : attributes;
    }

    // ---------------------------------------------------------------
    // Writing
    // ---------------------------------------------------------------

    /**
     * Writes text at the cursor position using current attributes,
     * overwriting existing cells. Wraps to the next line on line end;
     * scrolls up when the bottom of the screen is exceeded.
     */
    public void write(String text) {
        Objects.requireNonNull(text, "text must not be null");
        if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("text must not contain line separators (\\n or \\r)");
        }
        for (int i = 0; i < text.length(); i++) {
            dequeGet(screen, cursorRow)[cursorCol] = new Cell(text.charAt(i), currentAttributes);
            advanceCursor();
        }
    }

    private void advanceCursor() {
        cursorCol++;
        if (cursorCol == width) {
            cursorCol = 0;
            cursorRow++;
        }
        if (cursorRow == height) {
            scrollUp();
            cursorRow = height - 1;
        }
    }

    private void scrollUp() {
        Cell[] topRow = screen.removeFirst();
        screen.addLast(emptyRow());
        if (scrollbackMax > 0) {
            if (scrollback.size() >= scrollbackMax) {
                scrollback.removeFirst();
            }
            scrollback.addLast(topRow);
        }
    }

    // ---------------------------------------------------------------
    // Content access
    // ---------------------------------------------------------------

    /**
     * Returns the line as a string of {@code width} characters.
     */
    public String getLine(Area area, int row) {
        Cell[] cells = resolveRow(area, row);
        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : cells) {
            sb.append(cell.character());
        }
        return sb.toString();
    }

    /**
     * Returns the cell at ({@code row}, {@code col}) in {@code area}.
     */
    public Cell getCell(Area area, int row, int col) {
        Cell[] cells = resolveRow(area, row);
        checkColumn(col);
        return cells[col];
    }

    /**
     * Returns the attributes of the cell at ({@code row}, {@code col}) in {@code area}.
     */
    public Attributes getAttributes(Area area, int row, int col) {
        return getCell(area, row, col).attributes();
    }

    /** Returns the screen as a string, rows joined by {@code '\n'}. */
    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder(width * height + height);
        boolean first = true;
        for (Cell[] row : screen) {
            if (!first) sb.append('\n');
            appendRow(sb, row);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Returns scrollback (oldest first) followed by screen, rows joined by {@code '\n'}.
     * Identical to {@link #getScreenAsString()} when scrollback is empty.
     */
    public String getAllAsString() {
        if (scrollback.isEmpty()) {
            return getScreenAsString();
        }
        int totalRows = scrollback.size() + height;
        StringBuilder sb = new StringBuilder(width * totalRows + totalRows);
        for (Cell[] row : scrollback) {
            appendRow(sb, row);
            sb.append('\n');
        }
        sb.append(getScreenAsString());
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Cell[] emptyRow() {
        Cell[] row = new Cell[width];
        Arrays.fill(row, Cell.EMPTY);
        return row;
    }

    private Cell[] resolveRow(Area area, int row) {
        return switch (area) {
            case SCREEN -> {
                checkScreenRow(row);
                yield dequeGet(screen, row);
            }
            case SCROLLBACK -> {
                checkScrollbackRow(row);
                yield dequeGet(scrollback, row);
            }
        };
    }

    private static Cell[] dequeGet(Deque<Cell[]> deque, int index) {
        int i = 0;
        for (Cell[] row : deque) {
            if (i++ == index) return row;
        }
        throw new AssertionError("index " + index + " out of range, size=" + (i));
    }

    private void checkScreenRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException(
                    "screen row " + row + " out of bounds [0, " + height + ")");
        }
    }

    private void checkScrollbackRow(int row) {
        int size = scrollback.size();
        if (row < 0 || row >= size) {
            throw new IndexOutOfBoundsException(
                    "scrollback row " + row + " out of bounds [0, " + size + ")");
        }
    }

    private void checkColumn(int col) {
        if (col < 0 || col >= width) {
            throw new IndexOutOfBoundsException(
                    "column " + col + " out of bounds [0, " + width + ")");
        }
    }

    private void appendRow(StringBuilder sb, Cell[] row) {
        for (Cell cell : row) {
            sb.append(cell.character());
        }
    }

    private int clampCol(int col)  { return clamp(col, 0, width - 1); }
    private int clampRow(int row)  { return clamp(row, 0, height - 1); }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void checkNonNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0, got: " + n);
    }
}
