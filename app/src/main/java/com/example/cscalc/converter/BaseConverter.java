package com.example.cscalc.converter;

import com.example.cscalc.utils.InputValidator;

import java.math.BigInteger;
import java.util.Locale;

/**
 * All number-base conversion logic lives here (no Android code in this class,
 * so it can be tested on its own).
 */
public final class BaseConverter {
    private static final int MAX_EXPLANATION_TERMS = 32;

    private BaseConverter() {
    }

    /** Parses a signed integer in the selected base without a fixed-width limit. */
    public static BigInteger parseBigInteger(String text, NumberBase from) {
        if (from == null) {
            throw new IllegalArgumentException("Base is required");
        }
        InputValidator.ValidationError error = InputValidator.validate(text, from);
        if (error != InputValidator.ValidationError.NONE) {
            throw new NumberFormatException("Input is not a valid integer for the selected base");
        }
        return new BigInteger(text.trim(), from.getRadix());
    }

    /** Legacy fixed-width parser for callers that specifically need a long. */
    public static long parse(String text, NumberBase from) {
        return parseBigInteger(text, from).longValueExact();
    }

    /** Formats an arbitrary-size signed integer in the selected base. */
    public static String format(BigInteger value, NumberBase to) {
        if (value == null || to == null) {
            throw new IllegalArgumentException("A number and base are required");
        }
        String result = value.toString(to.getRadix());
        return to == NumberBase.HEXADECIMAL ? result.toUpperCase(Locale.ROOT) : result;
    }

    /** Formats a signed long value in the selected base. */
    public static String format(long value, NumberBase to) {
        return format(BigInteger.valueOf(value), to);
    }

    /** Convert text from one base to another. */
    public static String convert(String text, NumberBase from, NumberBase to) {
        return format(parseBigInteger(text, from), to);
    }

    /**
     * Describes the positional expansion of an input value. Long explanations
     * show the leading terms and state that the exact total uses BigInteger.
     */
    public static String explain(String text, NumberBase from) {
        BigInteger value = parseBigInteger(text, from);
        String normalized = text.trim().toUpperCase(Locale.ROOT);
        boolean negative = normalized.charAt(0) == '-';
        String digits = negative ? normalized.substring(1) : normalized;

        int firstNonZero = 0;
        while (firstNonZero < digits.length() && digits.charAt(firstNonZero) == '0') {
            firstNonZero++;
        }
        if (value.signum() == 0) {
            return "Zero in base " + from.getRadix() + " has value 0.";
        }

        int radix = from.getRadix();
        int termCount = digits.length() - firstNonZero;
        int displayedTerms = Math.min(termCount, MAX_EXPLANATION_TERMS);
        StringBuilder explanation = new StringBuilder();
        explanation.append("Place-value expansion (base ").append(radix).append("): ");
        if (negative) {
            explanation.append("−(");
        }
        for (int index = 0; index < displayedTerms; index++) {
            if (index > 0) {
                explanation.append(" + ");
            }
            int digitIndex = firstNonZero + index;
            int digit = Character.digit(digits.charAt(digitIndex), radix);
            int exponent = digits.length() - 1 - digitIndex;
            explanation.append(digit).append(" × ").append(radix).append('^').append(exponent);
        }
        if (termCount > displayedTerms) {
            explanation.append(" + … (")
                    .append(termCount - displayedTerms)
                    .append(" lower-place terms omitted)");
        }
        if (negative) {
            explanation.append(')');
        }
        explanation.append("\nThe exact integer value is ")
                .append(value.toString())
                .append("; each digit is multiplied by its base raised to its position.");
        return explanation.toString();
    }
}
