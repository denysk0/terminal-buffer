package com.example.terminalbuffer;

/**
 * 16 standard ANSI terminal colors + DEFAULT.
 * DEFAULT uses whatever the terminal's current default is, but
 * distinct from BLACK or BRIGHT_BLACK
 */
public enum TerminalColor {
    DEFAULT,
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE,
    BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW,
    BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYAN, BRIGHT_WHITE
}
