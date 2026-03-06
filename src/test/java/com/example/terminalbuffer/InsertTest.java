package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertTest {

    @Test
    void insertIntoEmptyRowPlacesText() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.insert("AB");
        assertEquals("AB   ", buf.getLine(Area.SCREEN, 0));
        assertEquals("     ", buf.getLine(Area.SCREEN, 1));
    }

    @Test
    void insertShiftsExistingContentRight() {
        var buf = new TerminalBuffer(10, 3, 0);
        buf.write("ABCDE");
        buf.setCursorPosition(2, 0);
        buf.insert("XY");
        assertEquals("ABXYCDE   ", buf.getLine(Area.SCREEN, 0));
    }

    @Test
    void insertSpillsOverflowToNextRow() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BB");
        buf.setCursorPosition(0, 0);
        buf.insert("XX");
        assertEquals("XXAAA", buf.getLine(Area.SCREEN, 0));
        assertEquals("AABB ", buf.getLine(Area.SCREEN, 1));
    }

    @Test
    void insertCascadesSpillAcrossMultipleRows() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCC");
        buf.setCursorPosition(0, 0);
        buf.insert("XX");
        assertEquals("XXAAA", buf.getLine(Area.SCREEN, 0));
        assertEquals("AABBB", buf.getLine(Area.SCREEN, 1));
        assertEquals("BBCCC", buf.getLine(Area.SCREEN, 2));
    }

    @Test
    void insertCascadeScrollsWhenAllRowsAreFull() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCC");
        buf.insert("D");
        buf.setCursorPosition(0, 0);
        buf.insert("XX");
        assertEquals("AABBB", buf.getLine(Area.SCREEN, 0));
        assertEquals("BBCCC", buf.getLine(Area.SCREEN, 1));
        assertEquals("CD   ", buf.getLine(Area.SCREEN, 2));
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("XXAAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void insertedCellsReceiveCurrentAttributes() {
        var buf = new TerminalBuffer(10, 3, 0);
        var red = new Attributes(TerminalColor.RED, TerminalColor.DEFAULT, 0);
        buf.setCurrentAttributes(red);
        buf.insert("AB");
        assertEquals(red, buf.getCell(Area.SCREEN, 0, 0).attributes());
        assertEquals(red, buf.getCell(Area.SCREEN, 0, 1).attributes());
    }

    @Test
    void shiftedCellsPreserveOriginalAttributes() {
        var buf = new TerminalBuffer(10, 3, 0);
        var blue = new Attributes(TerminalColor.BLUE, TerminalColor.DEFAULT, 0);
        var red  = new Attributes(TerminalColor.RED,  TerminalColor.DEFAULT, 0);
        buf.setCurrentAttributes(blue);
        buf.write("ABCDE");
        buf.setCursorPosition(2, 0);
        buf.setCurrentAttributes(red);
        buf.insert("XY");
        assertEquals(blue, buf.getCell(Area.SCREEN, 0, 0).attributes());
        assertEquals(blue, buf.getCell(Area.SCREEN, 0, 1).attributes());
        assertEquals(red,  buf.getCell(Area.SCREEN, 0, 2).attributes());
        assertEquals(red,  buf.getCell(Area.SCREEN, 0, 3).attributes());
        assertEquals(blue, buf.getCell(Area.SCREEN, 0, 4).attributes());
    }

    // ---------------------------------------------------------------
    // Cursor movement
    // ---------------------------------------------------------------

    @Test
    void insertMovesCursorByTextLength() {
        var buf = new TerminalBuffer(10, 3, 0);
        buf.setCursorPosition(3, 1);
        buf.insert("AB");
        assertEquals(5, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }

    @Test
    void insertWrapsCursorToNextLine() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCursorPosition(4, 0);
        buf.insert("AB");
        assertEquals(1, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }

    @Test
    void insertCursorAccountsForScrollDuringCascade() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.setCursorPosition(4, 2);
        buf.insert("AB");
        assertEquals(1, buf.getCursorCol());
        assertEquals(2, buf.getCursorRow());
    }
}
