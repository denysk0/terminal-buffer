package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearTest {

    private static TerminalBuffer filledBuffer() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCC");
        buf.insertEmptyLineAtBottom();
        return buf;
    }

    // ---------------------------------------------------------------
    // clearScreen
    // ---------------------------------------------------------------

    @Test
    void clearScreenEmptiesAllScreenRows() {
        var buf = filledBuffer();
        buf.clearScreen();
        for (int row = 0; row < buf.getHeight(); row++) {
            assertEquals("     ", buf.getLine(Area.SCREEN, row));
        }
    }

    @Test
    void clearScreenPreservesScrollback() {
        var buf = filledBuffer();
        buf.clearScreen();
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("AAAAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void clearScreenPreservesCursorPosition() {
        var buf = filledBuffer();
        buf.setCursorPosition(3, 1);
        buf.clearScreen();
        assertEquals(3, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }

    // ---------------------------------------------------------------
    // clearAll
    // ---------------------------------------------------------------

    @Test
    void clearAllEmptiesAllScreenRows() {
        var buf = filledBuffer();
        buf.clearAll();
        for (int row = 0; row < buf.getHeight(); row++) {
            assertEquals("     ", buf.getLine(Area.SCREEN, row));
        }
    }

    @Test
    void clearAllEmptiesScrollback() {
        var buf = filledBuffer();
        buf.clearAll();
        assertEquals(0, buf.getScrollbackSize());
    }

    @Test
    void clearAllPreservesCursorPosition() {
        var buf = filledBuffer();
        buf.setCursorPosition(3, 1);
        buf.clearAll();
        assertEquals(3, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }
}
