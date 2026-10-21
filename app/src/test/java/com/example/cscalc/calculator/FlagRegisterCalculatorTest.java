package com.example.cscalc.calculator;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FlagRegisterCalculatorTest {
    @Test
    public void intel8085AdditionWrapsAndSetsCarryAuxiliaryAndZero() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8085, 8,
                FlagRegisterCalculator.Operation.ADD, 255, 1);
        assertEquals(BigInteger.ZERO, result.getValue());
        assertTrue(result.getFlags().get("CY"));
        assertTrue(result.getFlags().get("AC"));
        assertTrue(result.getFlags().get("Z"));
        assertTrue(result.getFlags().get("P"));
    }

    @Test
    public void intel8085SubtractionSetsBorrowAndSign() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8085, 8,
                FlagRegisterCalculator.Operation.SUB, 0, 1);
        assertEquals(BigInteger.valueOf(255), result.getValue());
        assertTrue(result.getFlags().get("CY"));
        assertTrue(result.getFlags().get("AC"));
        assertTrue(result.getFlags().get("S"));
    }

    @Test
    public void intel8085AndSetsAuxiliaryCarryButClearsCarry() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8085, 8,
                FlagRegisterCalculator.Operation.AND, 240, 15);
        assertTrue(result.getFlags().get("AC"));
        assertFalse(result.getFlags().get("CY"));
    }

    @Test
    public void intel8086DistinguishesSignedOverflowFromUnsignedCarry() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8086, 8,
                FlagRegisterCalculator.Operation.ADD, 127, 1);
        assertEquals(BigInteger.valueOf(128), result.getValue());
        assertFalse(result.getFlags().get("CF"));
        assertTrue(result.getFlags().get("OF"));
        assertTrue(result.getFlags().get("SF"));
        assertFalse(result.getFlags().get("PF"));
        assertEquals(BigInteger.valueOf(-128), result.getSignedValue());
    }

    @Test
    public void intel8086MaximumUnsignedAdditionSetsCarryWithoutOverflow() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8086, 8,
                FlagRegisterCalculator.Operation.ADD, 255, 1);
        assertEquals(BigInteger.ZERO, result.getValue());
        assertTrue(result.getFlags().get("CF"));
        assertFalse(result.getFlags().get("OF"));
        assertTrue(result.getFlags().get("ZF"));
        assertTrue(result.getFlags().get("PF"));
    }

    @Test
    public void intel8086SubtractionSetsBorrowAndAuxiliaryBorrow() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8086, 8,
                FlagRegisterCalculator.Operation.SUB, 0, 1);
        assertTrue(result.getFlags().get("CF"));
        assertTrue(result.getFlags().get("AF"));
        assertFalse(result.getFlags().get("OF"));
    }

    @Test
    public void intel8086LogicalOperationMarksAuxiliaryFlagUndefined() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8086, 8,
                FlagRegisterCalculator.Operation.XOR, 170, 255);
        assertEquals(BigInteger.valueOf(85), result.getValue());
        assertFalse(result.getFlags().get("CF"));
        assertFalse(result.getFlags().get("OF"));
        assertNull(result.getFlags().get("AF"));
        assertTrue(result.getFlags().get("PF"));
    }

    @Test
    public void intel8086SupportsWordOperations() {
        FlagRegisterCalculator.Result result = calculate(
                FlagRegisterCalculator.Processor.INTEL_8086, 16,
                FlagRegisterCalculator.Operation.ADD, 65535, 1);
        assertEquals(BigInteger.ZERO, result.getValue());
        assertTrue(result.getFlags().get("CF"));
        assertTrue(result.getFlags().get("ZF"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidProcessorWidth() {
        calculate(FlagRegisterCalculator.Processor.INTEL_8085, 16,
                FlagRegisterCalculator.Operation.ADD, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfRangeOperands() {
        calculate(FlagRegisterCalculator.Processor.INTEL_8086, 8,
                FlagRegisterCalculator.Operation.ADD, 256, 1);
    }

    private FlagRegisterCalculator.Result calculate(
            FlagRegisterCalculator.Processor processor, int width,
            FlagRegisterCalculator.Operation operation, long a, long b) {
        return FlagRegisterCalculator.calculate(processor, width, operation,
                BigInteger.valueOf(a), BigInteger.valueOf(b));
    }
}
