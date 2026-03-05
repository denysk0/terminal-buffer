package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScrollTest {

    // ---------------------------------------------------------------
    // Basic scroll
    // ---------------------------------------------------------------

    @Test
    void writePastBottomScrollsScreenUp() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("D");
        assertEquals("BBBBB", buf.getLine(Area.SCREEN, 0));
        assertEquals("CCCCC", buf.getLine(Area.SCREEN, 1));
        assertEquals("D    ", buf.getLine(Area.SCREEN, 2));
    }

    @Test
    void scrolledLineMovesToScrollback() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("D");
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("AAAAA", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void newRowAfterScrollIsEmpty() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        assertEquals("     ", buf.getLine(Area.SCREEN, 2));
    }

    // ---------------------------------------------------------------
    // Scrollback order
    // ---------------------------------------------------------------

    @Test
    void scrollbackOrderIsOldestFirst() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("FIRST");
        buf.write("SECND");
        buf.write("THIRD");
        buf.write("FOURT");
        buf.write("D");
        assertEquals("FIRST", buf.getLine(Area.SCROLLBACK, 0));
        assertEquals("SECND", buf.getLine(Area.SCROLLBACK, 1));
    }

    @Test
    void getAllAsStringShowsScrollbackBeforeScreen() {
        var buf = new TerminalBuffer(5, 3, 100);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("D");
        String all = buf.getAllAsString();
        assertEquals("AAAAA\nBBBBB\nCCCCC\nD    ", all);
    }

    // ---------------------------------------------------------------
    // scrollbackMax cap
    // ---------------------------------------------------------------

    @Test
    void scrollbackMaxZeroDiscardsScrolledLines() {
        var buf = new TerminalBuffer(5, 3, 0);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("D");
        assertEquals("BBBBB", buf.getLine(Area.SCREEN, 0));
        assertEquals(0, buf.getScrollbackSize());
    }

    @Test
    void scrollbackMaxOnePersistsOnlyLatestScrolledLine() {
        var buf = new TerminalBuffer(5, 3, 1);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("DDDDD");
        buf.write("E");
        assertEquals(1, buf.getScrollbackSize());
        assertEquals("BBBBB", buf.getLine(Area.SCROLLBACK, 0));
    }

    @Test
    void scrollbackMaxCapIsRespected() {
        var buf = new TerminalBuffer(5, 3, 2);
        buf.write("AAAAA");
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("DDDDD");
        buf.write("EEEEE");
        buf.write("F");
        assertEquals(2, buf.getScrollbackSize());
        assertEquals("BBBBB", buf.getLine(Area.SCROLLBACK, 0));
        assertEquals("CCCCC", buf.getLine(Area.SCROLLBACK, 1));
    }

    // ---------------------------------------------------------------
    // Attributes preserved in scrollback
    // ---------------------------------------------------------------

    @Test
    void scrollPreservesAttributesInScrollback() {
        var buf = new TerminalBuffer(5, 3, 100);
        var boldRed = new Attributes(TerminalColor.RED, TerminalColor.DEFAULT, 0)
                .withStyle(Style.BOLD);
        buf.setCurrentAttributes(boldRed);
        buf.write("AAAAA");
        buf.setCurrentAttributes(Attributes.DEFAULT);
        buf.write("BBBBB");
        buf.write("CCCCC");
        buf.write("D");
        assertEquals(boldRed, buf.getAttributes(Area.SCROLLBACK, 0, 0));
    }
}
