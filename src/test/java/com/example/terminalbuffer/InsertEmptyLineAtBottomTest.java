package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertEmptyLineAtBottomTest {

    private static TerminalBuffer filledBuffer(int scrollbackMax) {
        var buf = new TerminalBuffer(5, 3, scrollbackMax);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCC");
        return buf;
    }

    @Test
    void topRowMovesToScrollback() {
        var buf = filledBuffer(100);
        buf.insertEmptyLineAtBottom();
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("AAAAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void bottomRowIsEmptyAfterInsert() {
        var buf = filledBuffer(100);
        buf.insertEmptyLineAtBottom();
        assertEquals("     ", buf.getLine(Area.SCREEN, 2));
    }

    @Test
    void screenShiftsUpByOne() {
        var buf = filledBuffer(100);
        buf.insertEmptyLineAtBottom();
        assertEquals("BBBBB", buf.getLine(Area.SCREEN, 0));
        assertEquals("CCC  ", buf.getLine(Area.SCREEN, 1));
    }

    @Test
    void cursorPositionIsUnchanged() {
        var buf = filledBuffer(100);
        buf.setCursorPosition(3, 2);
        buf.insertEmptyLineAtBottom();
        assertEquals(3, buf.getCursorCol());
        assertEquals(2, buf.getCursorRow());
    }

    @Test
    void scrollbackCapIsRespected() {
        var buf = filledBuffer(1);
        buf.insertEmptyLineAtBottom();
        buf.insertEmptyLineAtBottom();
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("BBBBB", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void insertWithZeroScrollbackDoesNotStoreHistory() {
        var buf = filledBuffer(0);
        buf.insertEmptyLineAtBottom();
        assertEquals(0, buf.getScrollbackSize());
        assertEquals("BBBBB", buf.getLine(Area.SCREEN, 0));
    }
}
