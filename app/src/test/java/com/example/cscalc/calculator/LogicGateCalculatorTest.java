package com.example.cscalc.calculator;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LogicGateCalculatorTest {
    @Test
    public void andTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.AND, false, false, false);
        assertGate(LogicGateCalculator.Gate.AND, false, true, false);
        assertGate(LogicGateCalculator.Gate.AND, true, false, false);
        assertGate(LogicGateCalculator.Gate.AND, true, true, true);
    }

    @Test
    public void orTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.OR, false, false, false);
        assertGate(LogicGateCalculator.Gate.OR, false, true, true);
        assertGate(LogicGateCalculator.Gate.OR, true, false, true);
        assertGate(LogicGateCalculator.Gate.OR, true, true, true);
    }

    @Test
    public void notTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.NOT, false, false, true);
        assertGate(LogicGateCalculator.Gate.NOT, true, false, false);
        List<LogicGateCalculator.TruthRow> rows =
                LogicGateCalculator.truthTable(LogicGateCalculator.Gate.NOT);
        assertEquals(2, rows.size());
    }

    @Test
    public void xorTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.XOR, false, false, false);
        assertGate(LogicGateCalculator.Gate.XOR, false, true, true);
        assertGate(LogicGateCalculator.Gate.XOR, true, false, true);
        assertGate(LogicGateCalculator.Gate.XOR, true, true, false);
    }

    @Test
    public void nandTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.NAND, false, false, true);
        assertGate(LogicGateCalculator.Gate.NAND, false, true, true);
        assertGate(LogicGateCalculator.Gate.NAND, true, false, true);
        assertGate(LogicGateCalculator.Gate.NAND, true, true, false);
    }

    @Test
    public void norTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.NOR, false, false, true);
        assertGate(LogicGateCalculator.Gate.NOR, false, true, false);
        assertGate(LogicGateCalculator.Gate.NOR, true, false, false);
        assertGate(LogicGateCalculator.Gate.NOR, true, true, false);
    }

    @Test
    public void xnorTruthTableAndOutputsAreCorrect() {
        assertGate(LogicGateCalculator.Gate.XNOR, false, false, true);
        assertGate(LogicGateCalculator.Gate.XNOR, false, true, false);
        assertGate(LogicGateCalculator.Gate.XNOR, true, false, false);
        assertGate(LogicGateCalculator.Gate.XNOR, true, true, true);
    }

    @Test
    public void reportsCorrectInputCountForNot() {
        assertEquals(1, LogicGateCalculator.Gate.NOT.getInputCount());
        assertEquals(2, LogicGateCalculator.Gate.XNOR.getInputCount());
    }

    private void assertGate(LogicGateCalculator.Gate gate, boolean a, boolean b,
                            boolean expected) {
        assertEquals(expected, LogicGateCalculator.evaluate(gate, a, b));
    }
}
