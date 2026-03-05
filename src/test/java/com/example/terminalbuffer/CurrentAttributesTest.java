package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrentAttributesTest {

    @Test
    void defaultCurrentAttributesIsDefault() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertEquals(Attributes.DEFAULT, buf.getCurrentAttributes());
    }

    @Test
    void setCurrentAttributesUpdatesState() {
        var buf = new TerminalBuffer(80, 24, 0);
        var attrs = new Attributes(TerminalColor.RED, TerminalColor.BLUE, 0).withStyle(Style.BOLD);
        buf.setCurrentAttributes(attrs);
        assertEquals(attrs, buf.getCurrentAttributes());
    }

    @Test
    void setCurrentAttributesNullFallsBackToDefault() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCurrentAttributes(null);
        assertEquals(Attributes.DEFAULT, buf.getCurrentAttributes());
    }
}
