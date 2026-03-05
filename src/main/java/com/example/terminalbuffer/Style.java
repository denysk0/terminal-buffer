package com.example.terminalbuffer;

/** Style flags stored as a bitmask (xxY bold, xYx italic, Yxx underline) inside {@link Attributes}. */
public enum Style {
    BOLD(1),
    ITALIC(2),
    UNDERLINE(4);

    private final int mask;

    Style(int mask) {
        this.mask = mask;
    }

    int mask() {
        return mask;
    }
}
