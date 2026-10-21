package com.example.cscalc.converter;

/** The four number systems supported by the converter. */
public enum NumberBase {
    BINARY(2, "BIN"),
    OCTAL(8, "OCT"),
    DECIMAL(10, "DEC"),
    HEXADECIMAL(16, "HEX");

    private final int radix;
    private final String shortName;

    NumberBase(int radix, String shortName) {
        this.radix = radix;
        this.shortName = shortName;
    }

    public int getRadix() {
        return radix;
    }

    public String getShortName() {
        return shortName;
    }
}
