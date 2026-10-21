package com.example.cscalc.utils;

import com.example.cscalc.converter.NumberBase;

/** Checks that typed text only uses digits allowed in the selected base. */
public final class InputValidator {
    public static final int MAX_INPUT_DIGITS = 4096;

    public enum ValidationError {
        NONE,
        EMPTY,
        TOO_LONG,
        INVALID_DIGIT
    }

    private InputValidator() {
    }

    public static ValidationError validate(String text, NumberBase base) {
        if (text == null || text.trim().isEmpty() || base == null) {
            return ValidationError.EMPTY;
        }
        String value = text.trim();
        int firstDigit = value.charAt(0) == '-' ? 1 : 0;
        if (firstDigit == value.length()) {
            return ValidationError.INVALID_DIGIT;
        }
        if (value.length() - firstDigit > MAX_INPUT_DIGITS) {
            return ValidationError.TOO_LONG;
        }
        for (int index = firstDigit; index < value.length(); index++) {
            if (!isDigitAllowed(value.charAt(index), base)) {
                return ValidationError.INVALID_DIGIT;
            }
        }
        return ValidationError.NONE;
    }

    public static boolean isValid(String text, NumberBase base) {
        return validate(text, base) == ValidationError.NONE;
    }

    public static boolean isDigitAllowed(char digit, NumberBase base) {
        if (base == null) {
            return false;
        }
        int value;
        if (digit >= '0' && digit <= '9') {
            value = digit - '0';
        } else if (digit >= 'A' && digit <= 'F') {
            value = digit - 'A' + 10;
        } else if (digit >= 'a' && digit <= 'f') {
            value = digit - 'a' + 10;
        } else {
            return false;
        }
        return value < base.getRadix();
    }
}
