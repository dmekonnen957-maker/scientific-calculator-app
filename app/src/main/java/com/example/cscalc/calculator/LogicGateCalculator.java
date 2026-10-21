package com.example.cscalc.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure Boolean logic for common one- and two-input gates. */
public final class LogicGateCalculator {
    private LogicGateCalculator() {
    }

    public enum Gate {
        AND("AND", 2, "A AND B"),
        OR("OR", 2, "A OR B"),
        NOT("NOT", 1, "NOT A"),
        XOR("XOR", 2, "A XOR B"),
        NAND("NAND", 2, "NOT (A AND B)"),
        NOR("NOR", 2, "NOT (A OR B)"),
        XNOR("XNOR", 2, "NOT (A XOR B)");

        private final String label;
        private final int inputCount;
        private final String expression;

        Gate(String label, int inputCount, String expression) {
            this.label = label;
            this.inputCount = inputCount;
            this.expression = expression;
        }

        public String getLabel() {
            return label;
        }

        public int getInputCount() {
            return inputCount;
        }

        public String getExpression() {
            return expression;
        }
    }

    public static boolean evaluate(Gate gate, boolean inputA, boolean inputB) {
        if (gate == null) {
            throw new IllegalArgumentException("Select a logic gate.");
        }
        switch (gate) {
            case AND:
                return inputA && inputB;
            case OR:
                return inputA || inputB;
            case NOT:
                return !inputA;
            case XOR:
                return inputA ^ inputB;
            case NAND:
                return !(inputA && inputB);
            case NOR:
                return !(inputA || inputB);
            case XNOR:
                return !(inputA ^ inputB);
            default:
                throw new IllegalStateException("Unsupported logic gate");
        }
    }

    public static List<TruthRow> truthTable(Gate gate) {
        if (gate == null) {
            throw new IllegalArgumentException("Select a logic gate.");
        }
        List<TruthRow> rows = new ArrayList<>();
        for (int a = 0; a <= 1; a++) {
            if (gate.getInputCount() == 1) {
                rows.add(new TruthRow(a == 1, false, evaluate(gate, a == 1, false)));
            } else {
                for (int b = 0; b <= 1; b++) {
                    rows.add(new TruthRow(a == 1, b == 1,
                            evaluate(gate, a == 1, b == 1)));
                }
            }
        }
        return Collections.unmodifiableList(rows);
    }

    public static String bit(boolean value) {
        return value ? "1" : "0";
    }

    public static final class TruthRow {
        private final boolean inputA;
        private final boolean inputB;
        private final boolean output;

        private TruthRow(boolean inputA, boolean inputB, boolean output) {
            this.inputA = inputA;
            this.inputB = inputB;
            this.output = output;
        }

        public boolean getInputA() {
            return inputA;
        }

        public boolean getInputB() {
            return inputB;
        }

        public boolean getOutput() {
            return output;
        }
    }
}
