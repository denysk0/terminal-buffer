package com.example.terminalbuffer;

/**
 * Immutable set of visual attributes for a single terminal cell.
 */
public record Attributes(TerminalColor foreground, TerminalColor background, int styleMask) {

    public static final Attributes DEFAULT =
            new Attributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, 0);

    public Attributes {
        foreground = (foreground == null) ? TerminalColor.DEFAULT : foreground;
        background = (background == null) ? TerminalColor.DEFAULT : background;
    }

    public boolean hasStyle(Style style) {
        return (styleMask & style.mask()) != 0;
    }

    public Attributes withStyle(Style style) {
        return new Attributes(foreground, background, styleMask | style.mask());
    }

    public Attributes withoutStyle(Style style) {
        return new Attributes(foreground, background, styleMask & ~style.mask());
    }
}
