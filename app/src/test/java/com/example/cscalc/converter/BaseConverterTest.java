package com.example.cscalc.converter;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaseConverterTest {
    @Test
    public void convertsBinaryInputToAllSupportedBases() {
        assertEquals("11111111", BaseConverter.convert("11111111", NumberBase.BINARY,
                NumberBase.BINARY));
        assertEquals("377", BaseConverter.convert("11111111", NumberBase.BINARY,
                NumberBase.OCTAL));
        assertEquals("255", BaseConverter.convert("11111111", NumberBase.BINARY,
                NumberBase.DECIMAL));
        assertEquals("FF", BaseConverter.convert("11111111", NumberBase.BINARY,
                NumberBase.HEXADECIMAL));
    }

    @Test
    public void parsesAndFormatsOctalValues() {
        assertEquals("11111111", BaseConverter.convert("377", NumberBase.OCTAL,
                NumberBase.BINARY));
        assertEquals("255", BaseConverter.convert("377", NumberBase.OCTAL,
                NumberBase.DECIMAL));
        assertEquals("FF", BaseConverter.convert("377", NumberBase.OCTAL,
                NumberBase.HEXADECIMAL));
    }

    @Test
    public void parsesAndFormatsDecimalValues() {
        assertEquals("11111111", BaseConverter.convert("255", NumberBase.DECIMAL,
                NumberBase.BINARY));
        assertEquals("377", BaseConverter.convert("255", NumberBase.DECIMAL,
                NumberBase.OCTAL));
        assertEquals("FF", BaseConverter.convert("255", NumberBase.DECIMAL,
                NumberBase.HEXADECIMAL));
    }

    @Test
    public void parsesAndFormatsHexadecimalValues() {
        assertEquals("11111111", BaseConverter.convert("ff", NumberBase.HEXADECIMAL,
                NumberBase.BINARY));
        assertEquals("377", BaseConverter.convert("FF", NumberBase.HEXADECIMAL,
                NumberBase.OCTAL));
        assertEquals("255", BaseConverter.convert("FF", NumberBase.HEXADECIMAL,
                NumberBase.DECIMAL));
    }

    @Test
    public void supportsNegativeValuesAndNegativeZero() {
        assertEquals("-11111111", BaseConverter.convert("-255", NumberBase.DECIMAL,
                NumberBase.BINARY));
        assertEquals("-FF", BaseConverter.convert("-377", NumberBase.OCTAL,
                NumberBase.HEXADECIMAL));
        assertEquals("0", BaseConverter.convert("-0", NumberBase.DECIMAL,
                NumberBase.BINARY));
    }

    @Test
    public void supportsZeroInEveryBase() {
        for (NumberBase base : NumberBase.values()) {
            assertEquals("0", BaseConverter.convert("0", base, NumberBase.DECIMAL));
        }
    }

    @Test
    public void supportsValuesLargerThanLong() {
        String value = "340282366920938463463374607431768211456";
        assertEquals(new BigInteger(value),
                BaseConverter.parseBigInteger(value, NumberBase.DECIMAL));
        assertEquals("100000000000000000000000000000000",
                BaseConverter.convert(value, NumberBase.DECIMAL, NumberBase.HEXADECIMAL));
    }

    @Test
    public void explainsPositionalWeights() {
        String explanation = BaseConverter.explain("101", NumberBase.BINARY);
        org.junit.Assert.assertTrue(explanation.contains("1 × 2^2"));
        org.junit.Assert.assertTrue(explanation.contains("0 × 2^1"));
        org.junit.Assert.assertTrue(explanation.contains("1 × 2^0"));
        assertEquals(true, BaseConverter.explain("00101", NumberBase.BINARY)
                .contains("1 × 2^2"));
    }

    @Test
    public void rejectsInvalidDigitsWithoutThrowingUnexpectedExceptions() {
        assertInvalid("102", NumberBase.BINARY);
        assertInvalid("8", NumberBase.OCTAL);
        assertInvalid("12A", NumberBase.DECIMAL);
        assertInvalid("FG", NumberBase.HEXADECIMAL);
    }

    @Test
    public void rejectsEmptyAndOverLimitInput() {
        assertInvalid("", NumberBase.DECIMAL);
        assertInvalid(repeat('1', 4097), NumberBase.BINARY);
    }

    private void assertInvalid(String text, NumberBase base) {
        try {
            BaseConverter.parseBigInteger(text, base);
            fail("Expected invalid input to be rejected");
        } catch (NumberFormatException expected) {
            // Expected validation failure.
        }
    }

    private String repeat(char character, int count) {
        StringBuilder value = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            value.append(character);
        }
        return value.toString();
    }
}
