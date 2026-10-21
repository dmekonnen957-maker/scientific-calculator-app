package com.example.cscalc.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BinaryArithmeticTest {

    @Test
    public void additionWithCarrySteps() {
        // 1011 (11) + 0111 (7) = 10010 (18)
        BinaryArithmetic.Result result = BinaryArithmetic.calculate(
                BinaryArithmetic.Operation.ADD, "1011", "0111");

        assertEquals("10010", result.getResultBinary());
        assertEquals("18", result.getResultDecimal());
        assertEquals("11", result.getOperandADecimal());
        assertEquals("7", result.getOperandBDecimal());
        assertNotNull(result.getCarryOrBorrowLine());
        assertTrue(result.getAlignedDisplay().contains("+"));
        assertTrue(result.getSteps().size() >= 4);
    }

    @Test
    public void additionZeroAndOne() {
        BinaryArithmetic.Result result = BinaryArithmetic.calculate(
                BinaryArithmetic.Operation.ADD, "0", "1");
        assertEquals("1", result.getResultBinary());
        assertEquals("1", result.getResultDecimal());
    }

    @Test
    public void subtractionWithBorrowSteps() {
        // 1100 (12) - 0101 (5) = 111 (7)
        BinaryArithmetic.Result result = BinaryArithmetic.calculate(
                BinaryArithmetic.Operation.SUBTRACT, "1100", "0101");

        assertEquals("111", result.getResultBinary());
        assertEquals("7", result.getResultDecimal());
        assertEquals("12", result.getOperandADecimal());
        assertEquals("5", result.getOperandBDecimal());
        assertNotNull(result.getCarryOrBorrowLine());
        assertTrue(result.getAlignedDisplay().contains("−"));
        assertTrue(result.getSteps().size() >= 4);
    }

    @Test
    public void subtractionNegativeResult() {
        // 0101 (5) - 1100 (12) = -7
        BinaryArithmetic.Result result = BinaryArithmetic.calculate(
                BinaryArithmetic.Operation.SUBTRACT, "0101", "1100");

        assertEquals("-111", result.getResultBinary());
        assertEquals("-7", result.getResultDecimal());
        assertTrue(result.getSteps().get(0).contains("smaller than"));
        assertTrue(result.getSteps().get(2).contains("Two's Complement"));
    }

    @Test
    public void multiplicationShiftAndAdd() {
        // 1010 (10) * 11 (3) = 11110 (30)
        BinaryArithmetic.Result result = BinaryArithmetic.calculate(
                BinaryArithmetic.Operation.MULTIPLY, "1010", "11");

        assertEquals("11110", result.getResultBinary());
        assertEquals("30", result.getResultDecimal());
        assertTrue(result.getAlignedDisplay().contains("×"));
        assertTrue(result.getSteps().size() >= 2);
    }

    @Test
    public void divisionWithQuotientAndRemainder() {
        // 1101 (13) / 10 (2) = 110 (6) rem 1
        BinaryArithmetic.Result result = BinaryArithmetic.calculate(
                BinaryArithmetic.Operation.DIVIDE, "1101", "10");

        assertEquals("110", result.getResultBinary());
        assertEquals("6", result.getResultDecimal());
        assertEquals("1", result.getRemainderBinary());
        assertEquals("1", result.getRemainderDecimal());
        assertTrue(result.getSteps().size() >= 4);
    }

    @Test
    public void divisionByZeroThrowsException() {
        try {
            BinaryArithmetic.calculate(BinaryArithmetic.Operation.DIVIDE, "1010", "0");
            fail("Expected ArithmeticException on division by zero");
        } catch (ArithmeticException e) {
            assertTrue(e.getMessage().contains("Division by zero"));
        }
    }

    @Test
    public void invalidBinaryCharactersRejected() {
        try {
            BinaryArithmetic.calculate(BinaryArithmetic.Operation.ADD, "10201", "10");
            fail("Expected IllegalArgumentException for invalid binary digit '2'");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid binary digit"));
        }
    }

    @Test
    public void emptyInputRejected() {
        try {
            BinaryArithmetic.calculate(BinaryArithmetic.Operation.ADD, "", "10");
            fail("Expected IllegalArgumentException for empty input");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be empty"));
        }
    }
}
