package com.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CursorTest {

    // ---------------------------------------------------------------
    // Initial state
    // ---------------------------------------------------------------

    @Test
    void initialCursorIsAtOrigin() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertEquals(new CursorPosition(0, 0), buf.getCursorPosition());
    }

    // ---------------------------------------------------------------
    // setCursorPosition - valid positions
    // ---------------------------------------------------------------

    @Test
    void setCursorPositionMovesToSpecifiedCell() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(5, 3);
        assertAll(
                () -> assertEquals(5, buf.getCursorCol()),
                () -> assertEquals(3, buf.getCursorRow())
        );
    }

    @Test
    void setCursorPositionAcceptsLastColumn() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(79, 0);
        assertEquals(79, buf.getCursorCol());
    }

    @Test
    void setCursorPositionAcceptsLastRow() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 23);
        assertEquals(23, buf.getCursorRow());
    }

    // ---------------------------------------------------------------
    // setCursorPosition - clamping
    // ---------------------------------------------------------------

    @Test
    void setCursorPositionClampsNegativeCol() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(-5, 3);
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void setCursorPositionClampsNegativeRow() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(5, -3);
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void setCursorPositionClampsColAtWidth() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(80, 0);
        assertEquals(79, buf.getCursorCol());
    }

    @Test
    void setCursorPositionClampsRowAtHeight() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 24);
        assertEquals(23, buf.getCursorRow());
    }

    @Test
    void setCursorPositionClampsLargeValues() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(1000, 1000);
        assertEquals(new CursorPosition(79, 23), buf.getCursorPosition());
    }

    // ---------------------------------------------------------------
    // moveRight
    // ---------------------------------------------------------------

    @Test
    void moveRightAdvancesCursorByN() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(10, 5);
        buf.moveRight(3);
        assertEquals(13, buf.getCursorCol());
    }

    @Test
    void moveRightByZeroIsNoop() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(10, 5);
        buf.moveRight(0);
        assertEquals(10, buf.getCursorCol());
    }

    @Test
    void moveRightClampsAtLastColumn() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(78, 0);
        buf.moveRight(10);
        assertEquals(79, buf.getCursorCol());
    }

    @Test
    void moveRightByMoreThanWidthClampsAtLastColumn() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.moveRight(200);
        assertEquals(79, buf.getCursorCol());
    }

    @Test
    void moveRightNegativeThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.moveRight(-1));
    }

    // ---------------------------------------------------------------
    // moveLeft
    // ---------------------------------------------------------------

    @Test
    void moveLeftRetreatesCursorByN() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(10, 5);
        buf.moveLeft(3);
        assertEquals(7, buf.getCursorCol());
    }

    @Test
    void moveLeftByZeroIsNoop() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(10, 5);
        buf.moveLeft(0);
        assertEquals(10, buf.getCursorCol());
    }

    @Test
    void moveLeftClampsAtFirstColumn() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(2, 0);
        buf.moveLeft(10);
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void moveLeftNegativeThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.moveLeft(-1));
    }

    // ---------------------------------------------------------------
    // moveDown
    // ---------------------------------------------------------------

    @Test
    void moveDownAdvancesCursorByN() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 5);
        buf.moveDown(4);
        assertEquals(9, buf.getCursorRow());
    }

    @Test
    void moveDownByZeroIsNoop() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 5);
        buf.moveDown(0);
        assertEquals(5, buf.getCursorRow());
    }

    @Test
    void moveDownClampsAtLastRow() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 22);
        buf.moveDown(10);
        assertEquals(23, buf.getCursorRow());
    }

    @Test
    void moveDownByMoreThanHeightClampsAtLastRow() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.moveDown(200);
        assertEquals(23, buf.getCursorRow());
    }

    @Test
    void moveDownNegativeThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.moveDown(-1));
    }

    // ---------------------------------------------------------------
    // moveUp
    // ---------------------------------------------------------------

    @Test
    void moveUpRetreatesCursorByN() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 10);
        buf.moveUp(4);
        assertEquals(6, buf.getCursorRow());
    }

    @Test
    void moveUpByZeroIsNoop() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 10);
        buf.moveUp(0);
        assertEquals(10, buf.getCursorRow());
    }

    @Test
    void moveUpClampsAtFirstRow() {
        var buf = new TerminalBuffer(80, 24, 0);
        buf.setCursorPosition(0, 2);
        buf.moveUp(10);
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void moveUpNegativeThrows() {
        var buf = new TerminalBuffer(80, 24, 0);
        assertThrows(IllegalArgumentException.class, () -> buf.moveUp(-1));
    }

    // ---------------------------------------------------------------
    // Cursor does not affect content
    // ---------------------------------------------------------------

    @Test
    void movingCursorDoesNotAlterCellContent() {
        var buf = new TerminalBuffer(10, 5, 0);
        String before = buf.getScreenAsString();
        buf.setCursorPosition(3, 2);
        buf.moveRight(2);
        buf.moveDown(1);
        assertEquals(before, buf.getScreenAsString());
    }
}
