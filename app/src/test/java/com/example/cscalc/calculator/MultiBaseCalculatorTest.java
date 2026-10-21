package com.example.cscalc.calculator;

import com.example.cscalc.converter.NumberBase;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MultiBaseCalculatorTest {
    @Test
    public void supportsAllOperatorsInBinaryMode() {
        assertResult("101 + 11", NumberBase.BINARY, "1000");
        assertResult("101 - 11", NumberBase.BINARY, "10");
        assertResult("101 * 11", NumberBase.BINARY, "1111");
        assertResult("1010 / 10", NumberBase.BINARY, "101");
    }

    @Test
    public void supportsAllOperatorsInOctalMode() {
        assertResult("17 + 3", NumberBase.OCTAL, "22");
        assertResult("17 - 3", NumberBase.OCTAL, "14");
        assertResult("7 * 6", NumberBase.OCTAL, "52");
        assertResult("34 / 4", NumberBase.OCTAL, "7");
    }

    @Test
    public void supportsAllOperatorsInDecimalMode() {
        assertResult("15 + 3", NumberBase.DECIMAL, "18");
        assertResult("15 - 3", NumberBase.DECIMAL, "12");
        assertResult("7 * 6", NumberBase.DECIMAL, "42");
        assertResult("35 / 4", NumberBase.DECIMAL, "8");
    }

    @Test
    public void supportsAllOperatorsInHexadecimalMode() {
        assertResult("F + 1", NumberBase.HEXADECIMAL, "10");
        assertResult("F - 1", NumberBase.HEXADECIMAL, "E");
        assertResult("7 * 6", NumberBase.HEXADECIMAL, "2A");
        assertResult("20 / 4", NumberBase.HEXADECIMAL, "8");
    }

    @Test
    public void observesPrecedenceParenthesesAndUnarySigns() {
        assertResult("2 + 3 * 4", NumberBase.DECIMAL, "14");
        assertResult("(2 + 3) * 4", NumberBase.DECIMAL, "20");
        assertResult("-7 / 3", NumberBase.DECIMAL, "-2");
        assertResult("-(2 + 3)", NumberBase.DECIMAL, "-5");
    }

    @Test
    public void supportsLargeExactIntegersAndDecimalEquivalent() {
        String expression = "123456789012345678901234567890 * 2";
        MultiBaseCalculator.Calculation calculation =
                MultiBaseCalculator.evaluate(expression, NumberBase.DECIMAL);
        assertEquals(new BigInteger("246913578024691357802469135780"),
                calculation.getResult());
        assertEquals("246913578024691357802469135780",
                calculation.getDecimalResult());
    }

    @Test
    public void reportsOperatorStepsAndIntegerDivision() {
        MultiBaseCalculator.Calculation calculation =
                MultiBaseCalculator.evaluate("10 / 3", NumberBase.DECIMAL);
        assertTrue(calculation.getExplanation().contains("10 ÷ 3 = 3"));
        assertTrue(calculation.getExplanation().contains("truncates toward zero"));
    }

    @Test
    public void rejectsDivisionByZero() {
        assertInvalid("4 / 0", NumberBase.DECIMAL, "Division by zero");
    }

    @Test
    public void rejectsMalformedExpressionsAndInvalidDigits() {
        assertInvalid("", NumberBase.DECIMAL, "Enter an expression");
        assertInvalid("2 +", NumberBase.DECIMAL, "Expected a number");
        assertInvalid("(2 + 3", NumberBase.DECIMAL, "Missing closing parenthesis");
        assertInvalid("2 3", NumberBase.DECIMAL, "Unexpected character");
        assertInvalid("102 + 1", NumberBase.BINARY, "Digit '2'");
        assertInvalid("G + 1", NumberBase.HEXADECIMAL, "Digit 'G'");
    }

    @Test
    public void rejectsExpressionsOverTheLengthLimit() {
        StringBuilder expression = new StringBuilder();
        for (int index = 0; index <= MultiBaseCalculator.MAX_EXPRESSION_LENGTH; index++) {
            expression.append('1');
        }
        assertInvalid(expression.toString(), NumberBase.BINARY, "too long");
    }

    @Test
    public void rejectsExcessivelyNestedExpressionsWithoutStackOverflow() {
        StringBuilder expression = new StringBuilder();
        for (int index = 0; index <= MultiBaseCalculator.MAX_NESTING_DEPTH; index++) {
            expression.append('(');
        }
        expression.append('1');
        for (int index = 0; index <= MultiBaseCalculator.MAX_NESTING_DEPTH; index++) {
            expression.append(')');
        }
        assertInvalid(expression.toString(), NumberBase.DECIMAL, "nesting is too deep");
    }

    @Test
    public void handlesLongUnarySignSequencesWithoutRecursiveParsing() {
        StringBuilder expression = new StringBuilder();
        for (int index = 0; index < 1000; index++) {
            expression.append('-');
        }
        expression.append('1');
        assertResult(expression.toString(), NumberBase.DECIMAL, "1");
    }

    private void assertResult(String expression, NumberBase base, String expected) {
        MultiBaseCalculator.Calculation calculation = MultiBaseCalculator.evaluate(expression, base);
        assertEquals(expected, calculation.getResultInBase());
    }

    private void assertInvalid(String expression, NumberBase base, String messagePart) {
        try {
            MultiBaseCalculator.evaluate(expression, base);
            fail("Expected the expression to be rejected");
        } catch (MultiBaseCalculator.ExpressionException exception) {
            assertTrue(exception.getMessage(), exception.getMessage().contains(messagePart));
        }
    }
}
