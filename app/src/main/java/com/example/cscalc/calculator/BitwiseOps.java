package com.example.cscalc.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Educational Bitwise Operations calculator supporting 8, 16, and 32-bit fixed widths.
 * Handles AND, OR, XOR, bounded NOT, left shift, logical right shift, and arithmetic right shift.
 * Pure Java (no Android framework dependencies) to facilitate unit testing.
 */
public final class BitwiseOps {

    public enum BitWidth {
        WIDTH_8(8, "8-bit (Byte)", 0xFFL, 0x80L, 2),
        WIDTH_16(16, "16-bit (Word)", 0xFFFFL, 0x8000L, 4),
        WIDTH_32(32, "32-bit (Dword)", 0xFFFFFFFFL, 0x80000000L, 8);

        private final int bits;
        private final String label;
        private final long mask;
        private final long signBitMask;
        private final int hexDigits;

        BitWidth(int bits, String label, long mask, long signBitMask, int hexDigits) {
            this.bits = bits;
            this.label = label;
            this.mask = mask;
            this.signBitMask = signBitMask;
            this.hexDigits = hexDigits;
        }

        public int getBits() {
            return bits;
        }

        public String getLabel() {
            return label;
        }

        public long getMask() {
            return mask;
        }

        public long getSignBitMask() {
            return signBitMask;
        }

        public int getHexDigits() {
            return hexDigits;
        }
    }

    public enum Operation {
        AND("AND", "&", false),
        OR("OR", "|", false),
        XOR("XOR", "^", false),
        NOT("NOT", "~", true),
        LEFT_SHIFT("Left Shift (<<)", "<<", false),
        RIGHT_SHIFT_LOGICAL("Logical Right Shift (>>>)", ">>>", false),
        RIGHT_SHIFT_ARITHMETIC("Arithmetic Right Shift (>>)", ">>", false);

        private final String displayName;
        private final String symbol;
        private final boolean unary;

        Operation(String displayName, String symbol, boolean unary) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.unary = unary;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean isUnary() {
            return unary;
        }
    }

    public static final class Result {
        private final Operation operation;
        private final BitWidth bitWidth;
        private final long operandA;
        private final Long operandB; // null for unary NOT
        private final long resultValue;
        private final String binaryA;
        private final String hexA;
        private final String binaryB;
        private final String hexB;
        private final String binaryResult;
        private final String hexResult;
        private final long unsignedResult;
        private final long signedResult;
        private final String maskingExplanation;
        private final String signednessExplanation;
        private final String operationExplanation;
        private final String bitGridComparison;
        private final List<String> steps;

        public Result(Operation operation, BitWidth bitWidth,
                      long operandA, Long operandB, long resultValue,
                      String binaryA, String hexA,
                      String binaryB, String hexB,
                      String binaryResult, String hexResult,
                      long unsignedResult, long signedResult,
                      String maskingExplanation,
                      String signednessExplanation,
                      String operationExplanation,
                      String bitGridComparison,
                      List<String> steps) {
            this.operation = operation;
            this.bitWidth = bitWidth;
            this.operandA = operandA;
            this.operandB = operandB;
            this.resultValue = resultValue;
            this.binaryA = binaryA;
            this.hexA = hexA;
            this.binaryB = binaryB;
            this.hexB = hexB;
            this.binaryResult = binaryResult;
            this.hexResult = hexResult;
            this.unsignedResult = unsignedResult;
            this.signedResult = signedResult;
            this.maskingExplanation = maskingExplanation;
            this.signednessExplanation = signednessExplanation;
            this.operationExplanation = operationExplanation;
            this.bitGridComparison = bitGridComparison;
            this.steps = steps;
        }

        public Operation getOperation() {
            return operation;
        }

        public BitWidth getBitWidth() {
            return bitWidth;
        }

        public long getOperandA() {
            return operandA;
        }

        public Long getOperandB() {
            return operandB;
        }

        public long getResultValue() {
            return resultValue;
        }

        public String getBinaryA() {
            return binaryA;
        }

        public String getHexA() {
            return hexA;
        }

        public String getBinaryB() {
            return binaryB;
        }

        public String getHexB() {
            return hexB;
        }

        public String getBinaryResult() {
            return binaryResult;
        }

        public String getHexResult() {
            return hexResult;
        }

        public long getUnsignedResult() {
            return unsignedResult;
        }

        public long getSignedResult() {
            return signedResult;
        }

        public String getMaskingExplanation() {
            return maskingExplanation;
        }

        public String getSignednessExplanation() {
            return signednessExplanation;
        }

        public String getOperationExplanation() {
            return operationExplanation;
        }

        public String getBitGridComparison() {
            return bitGridComparison;
        }

        public List<String> getSteps() {
            return steps;
        }
    }

    private BitwiseOps() {
    }

    /**
     * Parse an input string in decimal, hex (0x prefix or containing A-F), or binary (0b prefix or 0/1 only if requested).
     */
    public static long parseInput(String input, BitWidth width, int defaultRadix) {
        if (width == null) {
            throw new IllegalArgumentException("Bit width is required");
        }
        return parseValue(input, defaultRadix) & width.getMask();
    }

    public static long parseShiftAmount(String input, int defaultRadix) {
        long value = parseValue(input, defaultRadix);
        if (value < 0) {
            throw new IllegalArgumentException("Shift amount cannot be negative");
        }
        return value;
    }

    private static long parseValue(String input, int defaultRadix) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        String clean = input.trim();
        long val;
        try {
            if (clean.startsWith("0b") || clean.startsWith("0B")) {
                val = Long.parseLong(clean.substring(2), 2);
            } else if (clean.startsWith("0x") || clean.startsWith("0X")) {
                val = Long.parseLong(clean.substring(2), 16);
            } else if (defaultRadix == 2) {
                val = Long.parseLong(clean, 2);
            } else if (defaultRadix == 16) {
                val = Long.parseLong(clean, 16);
            } else {
                val = Long.parseLong(clean, 10);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for input: \"" + input + "\"");
        }
        return val;
    }

    public static Result calculate(Operation operation, BitWidth width, long valA, Long valB) {
        if (operation == null || width == null) {
            throw new IllegalArgumentException("Operation and bit width are required");
        }
        boolean shiftOperation = operation == Operation.LEFT_SHIFT
                || operation == Operation.RIGHT_SHIFT_LOGICAL
                || operation == Operation.RIGHT_SHIFT_ARITHMETIC;
        if (shiftOperation && valB != null && valB < 0) {
            throw new IllegalArgumentException("Shift amount cannot be negative");
        }
        long a = valA & width.getMask();
        Long b = (valB != null) ? (valB & width.getMask()) : null;
        long res;
        List<String> stepList = new ArrayList<>();

        switch (operation) {
            case AND:
                if (b == null) throw new IllegalArgumentException("Operand B is required for AND");
                res = a & b;
                stepList.add(String.format("Bitwise AND (A & B): Each result bit is 1 only if BOTH corresponding bits of A and B are 1."));
                break;
            case OR:
                if (b == null) throw new IllegalArgumentException("Operand B is required for OR");
                res = a | b;
                stepList.add(String.format("Bitwise OR (A | B): Each result bit is 1 if EITHER corresponding bit of A or B is 1."));
                break;
            case XOR:
                if (b == null) throw new IllegalArgumentException("Operand B is required for XOR");
                res = a ^ b;
                stepList.add(String.format("Bitwise XOR (A ^ B): Each result bit is 1 if the corresponding bits of A and B DIFFER (one is 1, the other is 0)."));
                break;
            case NOT:
                // Fixed-width NOT: masked to the selected bit width!
                // Unbounded ~a in Java (64-bit) would set all higher 64-W bits to 1.
                // We MUST mask with width.getMask() to avoid unbounded NOT.
                res = (~a) & width.getMask();
                stepList.add(String.format("Bitwise NOT (~A): Inverts every bit of A within the %d-bit boundary.", width.getBits()));
                stepList.add(String.format("Crucial: An unbounded NOT (~0x%s) in Java sets higher bits to 1s. Bounded to %d bits with mask 0x%s, the result is strictly 0x%s.",
                        formatHexRaw(a, width), width.getBits(), formatHexRaw(width.getMask(), width), formatHexRaw(res, width)));
                break;
            case LEFT_SHIFT: {
                if (b == null) throw new IllegalArgumentException("Shift amount is required for Left Shift");
                int shiftAmount = (int) Math.min(valB, width.getBits());
                res = shiftAmount >= width.getBits() ? 0 : (a << shiftAmount) & width.getMask();
                stepList.add(String.format("Left Shift (A << %d): Shifts bits left by %d positions. Vacated lower bits are filled with 0s.", valB, valB));
                stepList.add(String.format("Bits pushed past bit position %d are discarded by the %d-bit fixed-width mask.", width.getBits() - 1, width.getBits()));
                stepList.add(String.format("Multiplying effect: Each left shift by 1 position doubles the value (mod 2^%d).", width.getBits()));
                break;
            }
            case RIGHT_SHIFT_LOGICAL: {
                if (b == null) throw new IllegalArgumentException("Shift amount is required for Logical Right Shift");
                int shiftAmount = (int) Math.min(valB, width.getBits());
                res = shiftAmount >= width.getBits()
                        ? 0 : (a >>> shiftAmount) & width.getMask();
                stepList.add(String.format("Logical Right Shift (A >>> %d): Shifts bits right by %d positions. Vacated upper bit positions are always filled with 0s (zero fill).", valB, valB));
                stepList.add("Logical right shift treats the operand as an UNSIGNED bit pattern.");
                break;
            }
            case RIGHT_SHIFT_ARITHMETIC: {
                if (b == null) throw new IllegalArgumentException("Shift amount is required for Arithmetic Right Shift");
                int shiftAmount = (int) Math.min(valB, width.getBits());
                boolean msbSet = (a & width.getSignBitMask()) != 0;
                if (msbSet) {
                    long signedA = toSigned(a, width);
                    long shifted = signedA >> shiftAmount;
                    res = shifted & width.getMask();
                    stepList.add(String.format("Arithmetic Right Shift (A >> %d): MSB is 1 (negative in two's complement).", valB));
                    stepList.add(String.format("Sign extension: The sign bit (1) is replicated into the top %d vacated positions, preserving the negative sign.", shiftAmount));
                } else {
                    res = (a >> shiftAmount) & width.getMask();
                    stepList.add(String.format("Arithmetic Right Shift (A >> %d): MSB is 0 (positive). Zeroes fill the vacated top positions.", valB));
                }
                stepList.add("Dividing effect: Each arithmetic right shift by 1 position divides the signed value by 2 (floored).");
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

        long unsignedRes = res & width.getMask();
        long signedRes = toSigned(res, width);

        String maskingExp = String.format(Locale.ROOT,
                "Fixed-width masking: For %d-bit width, results are constrained by bitmask 0x%s (%s in binary). Any overflow beyond bit %d is discarded.",
                width.getBits(), formatHexRaw(width.getMask(), width), formatBinaryRaw(width.getMask(), width), width.getBits() - 1);

        boolean resultMsb = (res & width.getSignBitMask()) != 0;
        String signednessExp = String.format(Locale.ROOT,
                "Signedness (Two's Complement): Bit %d is the sign bit (MSB = %d). %s Unsigned = %d; Signed = %d (range [%d to %d]).",
                width.getBits() - 1,
                resultMsb ? 1 : 0,
                resultMsb ? "Since MSB=1, the value is NEGATIVE." : "Since MSB=0, the value is POSITIVE.",
                unsignedRes,
                signedRes,
                -(1L << (width.getBits() - 1)),
                (1L << (width.getBits() - 1)) - 1
        );

        String opExp = stepList.get(0);
        String grid = buildBitGrid(operation, width, a, b, res);

        return new Result(
                operation, width,
                a, shiftOperation ? valB : b, res,
                formatBinary(a, width), formatHex(a, width),
                (b != null) ? formatBinary(b, width) : null,
                (b != null) ? formatHex(b, width) : null,
                formatBinary(res, width), formatHex(res, width),
                unsignedRes, signedRes,
                maskingExp, signednessExp, opExp, grid, stepList
        );
    }

    public static long toSigned(long value, BitWidth width) {
        long v = value & width.getMask();
        if ((v & width.getSignBitMask()) != 0) {
            return v - (1L << width.getBits());
        }
        return v;
    }

    public static String formatBinaryRaw(long value, BitWidth width) {
        long v = value & width.getMask();
        StringBuilder sb = new StringBuilder();
        for (int i = width.getBits() - 1; i >= 0; i--) {
            sb.append((v & (1L << i)) != 0 ? '1' : '0');
        }
        return sb.toString();
    }

    public static String formatBinary(long value, BitWidth width) {
        String raw = formatBinaryRaw(value, width);
        // Group into 4-bit nibbles
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append(' ');
            }
            sb.append(raw.charAt(i));
        }
        return sb.toString();
    }

    public static String formatHexRaw(long value, BitWidth width) {
        long v = value & width.getMask();
        String format = "%0" + width.getHexDigits() + "X";
        return String.format(Locale.ROOT, format, v);
    }

    public static String formatHex(long value, BitWidth width) {
        return "0x" + formatHexRaw(value, width);
    }

    private static String buildBitGrid(Operation op, BitWidth width, long a, Long b, long res) {
        StringBuilder sb = new StringBuilder();
        String binA = formatBinary(a, width);
        String binRes = formatBinary(res, width);

        if (op.isUnary()) {
            sb.append("A:      ").append(binA).append("  (").append(formatHex(a, width)).append(")\n");
            sb.append("Op:     ~ (NOT)\n");
            sb.append("        ").append(repeat('-', binA.length())).append("\n");
            sb.append("Result: ").append(binRes).append("  (").append(formatHex(res, width)).append(")");
        } else if (op == Operation.LEFT_SHIFT || op == Operation.RIGHT_SHIFT_LOGICAL || op == Operation.RIGHT_SHIFT_ARITHMETIC) {
            sb.append("A:      ").append(binA).append("  (").append(formatHex(a, width)).append(")\n");
            sb.append("Shift:  ").append(op.getSymbol()).append(" ").append(b).append(" bit(s)\n");
            sb.append("        ").append(repeat('-', binA.length())).append("\n");
            sb.append("Result: ").append(binRes).append("  (").append(formatHex(res, width)).append(")");
        } else {
            String binB = formatBinary(b, width);
            sb.append("A:      ").append(binA).append("  (").append(formatHex(a, width)).append(")\n");
            sb.append("B:      ").append(binB).append("  (").append(formatHex(b, width)).append(")\n");
            sb.append("Op:     ").append(op.getSymbol()).append(" (").append(op.getDisplayName()).append(")\n");
            sb.append("        ").append(repeat('-', binA.length())).append("\n");
            sb.append("Result: ").append(binRes).append("  (").append(formatHex(res, width)).append(")");
        }
        return sb.toString();
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
