package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WriteTest {

    // ---------------------------------------------------------------
    // Input validation
    // ---------------------------------------------------------------

    @Test
    void writeNullThrows() {
        var buf = new TerminalBuffer(10, 5, 0);
        assertThrows(NullPointerException.class, () -> buf.write(null));
    }

    @Test
    void writeWithNewlineThrows() {
        var buf = new TerminalBuffer(10, 5, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.write("ab\ncd"));
    }

    @Test
    void writeWithCarriageReturnThrows() {
        var buf = new TerminalBuffer(10, 5, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.write("ab\rcd"));
    }

    // ---------------------------------------------------------------
    // No-op
    // ---------------------------------------------------------------

    @Test
    void writeEmptyStringIsNoop() {
        var buf = new TerminalBuffer(10, 5, 0);
        String before = buf.getScreenAsString();
        buf.write("");
        assertEquals(before, buf.getScreenAsString());
        assertEquals(0, buf.getCursorCol());
        assertEquals(0, buf.getCursorRow());
    }

    // ---------------------------------------------------------------
    // Basic write and overwrite
    // ---------------------------------------------------------------

    @Test
    void writeInsertsTextAtCursorPosition() {
        var buf = new TerminalBuffer(10, 5, 0);
        buf.write("abc");
        assertEquals("abc       ", buf.getLine(Area.SCREEN, 0));
    }

    @Test
    void writeOverwritesExistingContent() {
        var buf = new TerminalBuffer(10, 5, 0);
        buf.write("hello");
        buf.setCursorPosition(2, 0);
        buf.write("XY");
        assertEquals("heXYo     ", buf.getLine(Area.SCREEN, 0));
    }

    @Test
    void writeDoesNotAffectOtherRows() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("abc");
        assertEquals("     ", buf.getLine(Area.SCREEN, 1));
        assertEquals("     ", buf.getLine(Area.SCREEN, 2));
    }

    // ---------------------------------------------------------------
    // Cursor advancement
    // ---------------------------------------------------------------

    @Test
    void writeAdvancesCursorByTextLength() {
        var buf = new TerminalBuffer(10, 5, 0);
        buf.write("abc");
        assertEquals(3, buf.getCursorCol());
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void writeAtLastColumnWrapsToNextLine() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCursorPosition(4, 0);
        buf.write("X");
        assertEquals(0, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }

    @Test
    void writeWrapsAcrossLine() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("abcdefgh");
        assertEquals("abcde", buf.getLine(Area.SCREEN, 0));
        assertEquals("fgh  ", buf.getLine(Area.SCREEN, 1));
        assertEquals(3, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }

    // ---------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------

    @Test
    void writeAppliesCurrentAttributes() {
        var buf = new TerminalBuffer(10, 5, 0);
        var attrs = new Attributes(TerminalColor.RED, TerminalColor.BLUE, 0);
        buf.setCurrentAttributes(attrs);
        buf.write("hi");
        assertEquals(attrs, buf.getCell(Area.SCREEN, 0, 0).attributes());
        assertEquals(attrs, buf.getCell(Area.SCREEN, 0, 1).attributes());
    }

    @Test
    void writeUsesAttributesAtTimeOfWrite() {
        var buf = new TerminalBuffer(10, 5, 0);
        var red = new Attributes(TerminalColor.RED, TerminalColor.DEFAULT, 0);
        var blue = new Attributes(TerminalColor.BLUE, TerminalColor.DEFAULT, 0);
        buf.setCurrentAttributes(red);
        buf.write("a");
        buf.setCurrentAttributes(blue);
        buf.write("b");
        assertEquals(red, buf.getCell(Area.SCREEN, 0, 0).attributes());
        assertEquals(blue, buf.getCell(Area.SCREEN, 0, 1).attributes());
    }

    // ---------------------------------------------------------------
    // Bottom of screen - no scrolling yet
    // ---------------------------------------------------------------

    @Test
    void writeAtLastRowClampsOnWrap() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCursorPosition(3, 2);
        buf.write("XYZ");
        assertEquals("   XY", buf.getLine(Area.SCREEN, 2));
        assertEquals(0, buf.getCursorCol());
        assertEquals(2, buf.getCursorRow());
    }
}
