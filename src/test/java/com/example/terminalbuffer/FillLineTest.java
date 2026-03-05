package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FillLineTest {

    @Test
    void fillLineWritesCharAcrossEntireCurrentRow() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCursorPosition(0, 1);
        buf.fillLine('X');
        assertEquals("     ", buf.getLine(Area.SCREEN, 0));
        assertEquals("XXXXX", buf.getLine(Area.SCREEN, 1));
        assertEquals("     ", buf.getLine(Area.SCREEN, 2));
    }

    @Test
    void fillLineUsesCurrentAttributes() {
        var buf = new TerminalBuffer(5, 3, 0);
        var redBold = new Attributes(TerminalColor.RED, TerminalColor.DEFAULT, 0).withStyle(Style.BOLD);
        buf.setCurrentAttributes(redBold);
        buf.fillLine('X');
        for (int col = 0; col < 5; col++) {
            assertEquals(redBold, buf.getCell(Area.SCREEN, 0, col).attributes());
        }
    }

    @Test
    void fillLineDoesNotMoveCursor() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.setCursorPosition(2, 1);
        buf.fillLine('Z');
        assertEquals(2, buf.getCursorCol());
        assertEquals(1, buf.getCursorRow());
    }

    @Test
    void fillLineDoesNotAffectScrollback() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("D");
        assertEquals(1, buf.getScrollbackSize());
        buf.fillLine('X');
        assertEquals("AAAAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void fillLineOverwritesExistingContent() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("ABCDE");
        buf.setCursorPosition(0, 0);
        buf.fillLine(' ');
        assertEquals("     ", buf.getLine(Area.SCREEN, 0));
    }
}
