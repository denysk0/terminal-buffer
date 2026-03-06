package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResizeTest {

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

    @Test
    void resizeWithZeroWidthThrows() {
        var buf = new TerminalBuffer(5, 3, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.resize(0, 3));
    }

    @Test
    void resizeWithNegativeWidthThrows() {
        var buf = new TerminalBuffer(5, 3, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.resize(-1, 3));
    }

    @Test
    void resizeWithZeroHeightThrows() {
        var buf = new TerminalBuffer(5, 3, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.resize(5, 0));
    }

    @Test
    void resizeWithNegativeHeightThrows() {
        var buf = new TerminalBuffer(5, 3, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.resize(5, -1));
    }

    // ---------------------------------------------------------------
    // Height increase
    // ---------------------------------------------------------------

    @Test
    void heightIncreaseAddsEmptyRowsAtBottom() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCC");
        buf.resize(5, 5);
        assertEquals(5, buf.getHeight());
        assertEquals("AAAAA", buf.getLine(Area.SCREEN, 0));
        assertEquals("BBBBB", buf.getLine(Area.SCREEN, 1));
        assertEquals("CCC  ", buf.getLine(Area.SCREEN, 2));
        assertEquals("     ", buf.getLine(Area.SCREEN, 3));
        assertEquals("     ", buf.getLine(Area.SCREEN, 4));
    }

    @Test
    void heightIncreaseDoesNotAffectScrollback() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCC");
        buf.insertEmptyLineAtBottom();
        buf.resize(5, 5);
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("AAAAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    // ---------------------------------------------------------------
    // Height decrease
    // ---------------------------------------------------------------

    @Test
    void heightDecreaseMovesTopRowsToScrollback() {
        var buf = new TerminalBuffer(5, 5, 100);
        for (int r = 0; r < 5; r++) {
            buf.setCursorPosition(0, r);
            buf.write("ROW" + r);
        }
        buf.resize(5, 3);
        assertEquals(3, buf.getHeight());
        assertEquals(2, buf.getScrollbackSize());
        assertEquals("ROW0 ", buf.getLine(Area.SCROLLBACK, 0));
        assertEquals("ROW1 ", buf.getLine(Area.SCROLLBACK, 1));
        assertEquals("ROW2 ", buf.getLine(Area.SCREEN, 0));
        assertEquals("ROW3 ", buf.getLine(Area.SCREEN, 1));
        assertEquals("ROW4 ", buf.getLine(Area.SCREEN, 2));
    }

    @Test
    void heightDecreaseRespectsScrollbackMax() {
        var buf = new TerminalBuffer(5, 5, 1);
        for (int r = 0; r < 5; r++) {
            buf.setCursorPosition(0, r);
            buf.write("ROW" + r);
        }
        buf.resize(5, 3);
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("ROW1 ", buf.getLine(Area.SCROLLBACK, 0));
    }

    // ---------------------------------------------------------------
    // Width increase
    // ---------------------------------------------------------------

    @Test
    void widthIncreasePadsRowsWithEmptyCells() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("ABCDE");
        buf.resize(8, 3);
        assertEquals(8, buf.getWidth());
        assertEquals("ABCDE   ", buf.getLine(Area.SCREEN, 0));
    }

    @Test
    void widthIncreaseNewCellsHaveDefaultAttributes() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCurrentAttributes(new Attributes(TerminalColor.RED, TerminalColor.DEFAULT, 0));
        buf.write("ABCDE");
        buf.resize(7, 3);
        assertEquals(Attributes.DEFAULT, buf.getCell(Area.SCREEN, 0, 5).attributes());
        assertEquals(Attributes.DEFAULT, buf.getCell(Area.SCREEN, 0, 6).attributes());
    }

    // ---------------------------------------------------------------
    // Width decrease
    // ---------------------------------------------------------------

    @Test
    void widthDecreaseTruncatesRowsOnRight() {
        var buf = new TerminalBuffer(8, 3, 0);
        buf.write("ABCDEFGH");
        buf.resize(5, 3);
        assertEquals(5, buf.getWidth());
        assertEquals("ABCDE", buf.getLine(Area.SCREEN, 0));
    }

    @Test
    void widthDecreasePreservesAttributesOfRemainingCells() {
        var buf = new TerminalBuffer(8, 3, 0);
        var blue = new Attributes(TerminalColor.BLUE, TerminalColor.DEFAULT, 0);
        buf.setCurrentAttributes(blue);
        buf.write("ABCDEFGH");
        buf.resize(5, 3);
        for (int col = 0; col < 5; col++) {
            assertEquals(blue, buf.getCell(Area.SCREEN, 0, col).attributes());
        }
    }

    // ---------------------------------------------------------------
    // Width resize affects scrollback
    // ---------------------------------------------------------------

    @Test
    void widthResizeAlsoAffectsScrollbackRows() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCC");
        buf.insertEmptyLineAtBottom();

        buf.resize(8, 3);
        assertEquals("AAAAA   ", buf.getLine(Area.SCROLLBACK, 0));

        buf.resize(3, 3);
        assertEquals("AAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    // ---------------------------------------------------------------
    // Combined width + height change
    // ---------------------------------------------------------------

    @Test
    void simultaneousWidthAndHeightChangePreservesContentAndPads() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.resize(8, 5);
        assertEquals(8, buf.getWidth());
        assertEquals(5, buf.getHeight());
        assertEquals("AAAAA   ", buf.getLine(Area.SCREEN, 0));
        assertEquals("BBBBB   ", buf.getLine(Area.SCREEN, 1));
        assertEquals("        ", buf.getLine(Area.SCREEN, 2));
        assertEquals("        ", buf.getLine(Area.SCREEN, 3));
        assertEquals("        ", buf.getLine(Area.SCREEN, 4));
    }

    // ---------------------------------------------------------------
    // Cursor clamping
    // ---------------------------------------------------------------

    @Test
    void cursorClampedWhenShrinkingBelowCursorPosition() {
        var buf = new TerminalBuffer(10, 5, 0);
        buf.setCursorPosition(8, 4);
        buf.resize(5, 3);
        assertEquals(4, buf.getCursorCol());
        assertEquals(2, buf.getCursorRow());
    }

    @Test
    void cursorUnchangedWhenGrowingAndAlreadyValid() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCursorPosition(3, 1);
        buf.resize(10, 6);
        assertEquals(3, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }
}
