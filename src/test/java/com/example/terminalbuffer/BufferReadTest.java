package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers: constructor validation, screen dimensions, empty line content,
 * cell/attribute access, out-of-bounds behaviour, and scrollback access
 * when scrollback is empty
 */
class BufferReadTest {

    // ---------------------------------------------------------------
    // Constructor validation
    // ---------------------------------------------------------------

    @Test
    void constructorRejectsZeroWidth() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 24, 100));
    }

    @Test
    void constructorRejectsNegativeWidth() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(-1, 24, 100));
    }

    @Test
    void constructorRejectsZeroHeight() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 0, 100));
    }

    @Test
    void constructorRejectsNegativeHeight() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, -1, 100));
    }

    @Test
    void constructorRejectsNegativeScrollbackMax() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 24, -1));
    }

    @Test
    void constructorAcceptsZeroScrollbackMax() {
        assertDoesNotThrow(() -> new TerminalBuffer(80, 24, 0));
    }

    @Test
    void constructorAcceptsMinimalDimensions() {
        assertDoesNotThrow(() -> new TerminalBuffer(1, 1, 0));
    }

    // ---------------------------------------------------------------
    // Dimension accessors
    // ---------------------------------------------------------------

    @Test
    void dimensionsAreStoredCorrectly() {
        var buf = new TerminalBuffer(80, 24, 500);
        assertAll(
                () -> assertEquals(80, buf.getWidth()),
                () -> assertEquals(24, buf.getHeight()),
                () -> assertEquals(500, buf.getScrollbackMax())
        );
    }

    // ---------------------------------------------------------------
    // Empty screen line content
    // ---------------------------------------------------------------

    @Test
    void emptyLineConsistsOfSpaces() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertEquals(" ".repeat(80), buf.getLine(Area.SCREEN, 0));
    }

    @Test
    void emptyLineHasExactlyWidthCharacters() {
        var buf = new TerminalBuffer(7, 3, 0);
        assertEquals(7, buf.getLine(Area.SCREEN, 0).length());
    }

    @Test
    void allScreenRowsAreEmptyOnConstruction() {
        var buf = new TerminalBuffer(5, 3, 0);
        for (int row = 0; row < 3; row++) {
            assertEquals("     ", buf.getLine(Area.SCREEN, row),
                    "row " + row + " should be blank");
        }
    }

    @Test
    void lastScreenRowIsAccessible() {
        var buf = new TerminalBuffer(4, 10, 0);
        assertDoesNotThrow(() -> buf.getLine(Area.SCREEN, 9));
    }

    // ---------------------------------------------------------------
    // getLine - out of bounds
    // ---------------------------------------------------------------

    @Test
    void getLineScreenNegativeRowThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getLine(Area.SCREEN, -1));
    }

    @Test
    void getLineScreenRowAtHeightThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getLine(Area.SCREEN, 24));
    }

    @Test
    void getLineScrollbackWhenEmptyThrows() {
        var buf = new TerminalBuffer(80, 24, 100);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getLine(Area.SCROLLBACK, 0));
    }

    // ---------------------------------------------------------------
    // getScreenAsString / getAllAsString
    // ---------------------------------------------------------------

    @Test
    void getScreenAsStringJoinsRowsWithNewline() {
        var buf = new TerminalBuffer(3, 2, 0);
        assertEquals("   \n   ", buf.getScreenAsString());
    }

    @Test
    void getAllAsStringEqualsScreenAsStringWhenScrollbackIsEmpty() {
        var buf = new TerminalBuffer(5, 3, 100);
        assertEquals(buf.getScreenAsString(), buf.getAllAsString());
    }

    // ---------------------------------------------------------------
    // getCell - content and defaults
    // ---------------------------------------------------------------

    @Test
    void emptyCellHasSpaceCharacter() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertEquals(' ', buf.getCell(Area.SCREEN, 0, 0).character());
    }

    @Test
    void emptyCellHasDefaultAttributes() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertEquals(Attributes.DEFAULT, buf.getCell(Area.SCREEN, 0, 0).attributes());
    }

    @Test
    void lastCellIsAccessible() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertDoesNotThrow(() -> buf.getCell(Area.SCREEN, 23, 79));
    }

    // ---------------------------------------------------------------
    // getCell - out of bounds
    // ---------------------------------------------------------------

    @Test
    void getCellNegativeRowThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCell(Area.SCREEN, -1, 0));
    }

    @Test
    void getCellRowAtHeightThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCell(Area.SCREEN, 24, 0));
    }

    @Test
    void getCellNegativeColumnThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCell(Area.SCREEN, 0, -1));
    }

    @Test
    void getCellColumnAtWidthThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCell(Area.SCREEN, 0, 80));
    }

    @Test
    void getCellScrollbackWhenEmptyThrows() {
        var buf = new TerminalBuffer(80, 24, 100);
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCell(Area.SCROLLBACK, 0, 0));
    }

    // ---------------------------------------------------------------
    // Default Attributes
    // ---------------------------------------------------------------

    @Test
    void getAttributesReturnsDefaultForEmptyCell() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertEquals(Attributes.DEFAULT, buf.getAttributes(Area.SCREEN, 0, 0));
    }

    @Test
    void defaultAttributesForegroundIsDefault() {
        assertEquals(TerminalColor.DEFAULT, Attributes.DEFAULT.foreground());
    }

    @Test
    void defaultAttributesBackgroundIsDefault() {
        assertEquals(TerminalColor.DEFAULT, Attributes.DEFAULT.background());
    }

    @Test
    void defaultAttributesHaveNoBoldStyle() {
        assertFalse(Attributes.DEFAULT.hasStyle(Style.BOLD));
    }

    @Test
    void defaultAttributesHaveNoItalicStyle() {
        assertFalse(Attributes.DEFAULT.hasStyle(Style.ITALIC));
    }

    @Test
    void defaultAttributesHaveNoUnderlineStyle() {
        assertFalse(Attributes.DEFAULT.hasStyle(Style.UNDERLINE));
    }
}
