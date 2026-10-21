package com.example.cscalc.calculator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Processor-specific status flag rules for the supported 8085/8086 operations. */
public final class FlagRegisterCalculator {
    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger FIFTEEN = BigInteger.valueOf(15);

    private FlagRegisterCalculator() {
    }

    public enum Processor {
        INTEL_8085("Intel 8085"),
        INTEL_8086("Intel 8086");

        private final String label;

        Processor(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Operation {
        ADD("ADD"),
        SUB("SUB"),
        CMP("CMP"),
        AND("AND"),
        OR("OR"),
        XOR("XOR");

        private final String label;

        Operation(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final class Result {
        private final Processor processor;
        private final Operation operation;
        private final int width;
        private final BigInteger operandA;
        private final BigInteger operandB;
        private final BigInteger value;
        private final Map<String, Boolean> flags;
        private final boolean compareOnly;

        private Result(Processor processor, Operation operation, int width,
                       BigInteger operandA, BigInteger operandB, BigInteger value,
                       Map<String, Boolean> flags, boolean compareOnly) {
            this.processor = processor;
            this.operation = operation;
            this.width = width;
            this.operandA = operandA;
            this.operandB = operandB;
            this.value = value;
            this.flags = Collections.unmodifiableMap(new LinkedHashMap<>(flags));
            this.compareOnly = compareOnly;
        }

        public Processor getProcessor() { return processor; }
        public Operation getOperation() { return operation; }
        public int getWidth() { return width; }
        public BigInteger getOperandA() { return operandA; }
        public BigInteger getOperandB() { return operandB; }
        public BigInteger getValue() { return value; }
        public Map<String, Boolean> getFlags() { return flags; }
        public boolean isCompareOnly() { return compareOnly; }

        public BigInteger getSignedValue() {
            return value.testBit(width - 1) ? value.subtract(ONE.shiftLeft(width)) : value;
        }
    }

    /**
     * Inputs may be signed decimal values or unsigned register values. Values are
     * represented in the selected width using two's-complement masking.
     */
    public static Result calculate(Processor processor, int width, Operation operation,
                                   BigInteger inputA, BigInteger inputB) {
        if (processor == null || operation == null || inputA == null || inputB == null) {
            throw new IllegalArgumentException("Choose a processor, operation, and both operands.");
        }
        if (processor == Processor.INTEL_8085 && width != 8) {
            throw new IllegalArgumentException("Intel 8085 supports an 8-bit operand width.");
        }
        if (processor == Processor.INTEL_8086 && width != 8 && width != 16) {
            throw new IllegalArgumentException("Intel 8086 supports 8-bit and 16-bit operands.");
        }
        BigInteger signedMinimum = ONE.shiftLeft(width - 1).negate();
        BigInteger unsignedMaximum = ONE.shiftLeft(width).subtract(ONE);
        if (inputA.compareTo(signedMinimum) < 0 || inputA.compareTo(unsignedMaximum) > 0
                || inputB.compareTo(signedMinimum) < 0 || inputB.compareTo(unsignedMaximum) > 0) {
            throw new IllegalArgumentException(
                    "Operands must fit the selected width as signed or unsigned integers.");
        }

        BigInteger mask = unsignedMaximum;
        BigInteger a = inputA.and(mask);
        BigInteger b = inputB.and(mask);
        BigInteger raw;
        switch (operation) {
            case ADD:
                raw = a.add(b);
                break;
            case SUB:
            case CMP:
                raw = a.subtract(b);
                break;
            case AND:
                raw = a.and(b);
                break;
            case OR:
                raw = a.or(b);
                break;
            case XOR:
                raw = a.xor(b);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation.");
        }
        BigInteger value = raw.and(mask);
        Map<String, Boolean> flags = processor == Processor.INTEL_8085
                ? calculate8085Flags(operation, a, b, raw, value, width, mask)
                : calculate8086Flags(operation, a, b, raw, value, width, mask);
        return new Result(processor, operation, width, a, b, value, flags,
                operation == Operation.CMP);
    }

    private static Map<String, Boolean> calculate8085Flags(Operation operation,
            BigInteger a, BigInteger b, BigInteger raw, BigInteger value,
            int width, BigInteger mask) {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put("S", value.testBit(width - 1));
        flags.put("Z", value.equals(ZERO));
        flags.put("P", hasEvenParity(value, 8));

        if (operation == Operation.ADD) {
            flags.put("AC", a.and(FIFTEEN).add(b.and(FIFTEEN)).compareTo(FIFTEEN) > 0);
            flags.put("CY", raw.compareTo(mask) > 0);
        } else if (operation == Operation.SUB || operation == Operation.CMP) {
            flags.put("AC", a.and(FIFTEEN).compareTo(b.and(FIFTEEN)) < 0);
            flags.put("CY", a.compareTo(b) < 0);
        } else {
            // 8085 ANA sets AC; ORA and XRA clear it. All three clear CY.
            flags.put("AC", operation == Operation.AND);
            flags.put("CY", false);
        }
        return flags;
    }

    private static Map<String, Boolean> calculate8086Flags(Operation operation,
            BigInteger a, BigInteger b, BigInteger raw, BigInteger value,
            int width, BigInteger mask) {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        boolean arithmetic = operation == Operation.ADD || operation == Operation.SUB
                || operation == Operation.CMP;
        boolean addition = operation == Operation.ADD;
        boolean subtraction = operation == Operation.SUB || operation == Operation.CMP;
        boolean carryOrBorrow = addition ? raw.compareTo(mask) > 0
                : subtraction && a.compareTo(b) < 0;
        boolean auxiliary = addition
                ? a.and(FIFTEEN).add(b.and(FIFTEEN)).compareTo(FIFTEEN) > 0
                : subtraction && a.and(FIFTEEN).compareTo(b.and(FIFTEEN)) < 0;
        boolean overflow = false;
        boolean signA = a.testBit(width - 1);
        boolean signB = b.testBit(width - 1);
        boolean signResult = value.testBit(width - 1);
        if (addition) {
            overflow = signA == signB && signResult != signA;
        } else if (subtraction) {
            overflow = signA != signB && signResult != signA;
        }

        flags.put("CF", arithmetic ? carryOrBorrow : false);
        flags.put("PF", hasEvenParity(value, 8));
        flags.put("AF", arithmetic ? auxiliary : null);
        flags.put("ZF", value.equals(ZERO));
        flags.put("SF", signResult);
        flags.put("OF", overflow);
        return flags;
    }

    private static boolean hasEvenParity(BigInteger value, int bits) {
        return value.and(ONE.shiftLeft(bits).subtract(ONE)).bitCount() % 2 == 0;
    }
}
