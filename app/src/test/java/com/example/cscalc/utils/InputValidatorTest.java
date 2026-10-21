package com.example.cscalc.utils;

import com.example.cscalc.converter.NumberBase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputValidatorTest {
    @Test
    public void acceptsDigitsAllowedInEachBase() {
        assertTrue(InputValidator.isValid("-10101", NumberBase.BINARY));
        assertTrue(InputValidator.isValid("755", NumberBase.OCTAL));
        assertTrue(InputValidator.isValid("-1234567890", NumberBase.DECIMAL));
        assertTrue(InputValidator.isValid("aB09", NumberBase.HEXADECIMAL));
    }

    @Test
    public void rejectsDigitsNotAllowedInSelectedBase() {
        assertFalse(InputValidator.isValid("102", NumberBase.BINARY));
        assertFalse(InputValidator.isValid("8", NumberBase.OCTAL));
        assertFalse(InputValidator.isValid("12A", NumberBase.DECIMAL));
        assertFalse(InputValidator.isValid("FG", NumberBase.HEXADECIMAL));
    }

    @Test
    public void reportsEmptyAndOversizedInputs() {
        assertEquals(InputValidator.ValidationError.EMPTY,
                InputValidator.validate("  ", NumberBase.DECIMAL));
        assertEquals(InputValidator.ValidationError.TOO_LONG,
                InputValidator.validate(repeat('1', InputValidator.MAX_INPUT_DIGITS + 1),
                        NumberBase.BINARY));
    }

    private String repeat(char character, int count) {
        StringBuilder value = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            value.append(character);
        }
        return value.toString();
    }
}
