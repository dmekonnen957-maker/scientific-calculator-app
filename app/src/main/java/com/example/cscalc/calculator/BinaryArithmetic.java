package com.example.cscalc.calculator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Educational binary arithmetic calculations with step-by-step carry, borrow,
 * partial product, and division breakdown.
 * Pure Java (no Android framework dependencies) to facilitate unit testing.
 */
public final class BinaryArithmetic {

    public enum Operation {
        ADD("Addition", "+"),
        SUBTRACT("Subtraction", "−"),
        MULTIPLY("Multiplication", "×"),
        DIVIDE("Division", "÷");

        private final String displayName;
        private final String symbol;

        Operation(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public static final class Result {
        private final Operation operation;
        private final String operandABinary;
        private final String operandADecimal;
        private final String operandBBinary;
        private final String operandBDecimal;
        private final String resultBinary;
        private final String resultDecimal;
        private final String remainderBinary;
        private final String remainderDecimal;
        private final String carryOrBorrowLine;
        private final String alignedDisplay;
        private final List<String> steps;

        public Result(Operation operation,
                      String operandABinary, String operandADecimal,
                      String operandBBinary, String operandBDecimal,
                      String resultBinary, String resultDecimal,
                      String remainderBinary, String remainderDecimal,
                      String carryOrBorrowLine,
                      String alignedDisplay,
                      List<String> steps) {
            this.operation = operation;
            this.operandABinary = operandABinary;
            this.operandADecimal = operandADecimal;
            this.operandBBinary = operandBBinary;
            this.operandBDecimal = operandBDecimal;
            this.resultBinary = resultBinary;
            this.resultDecimal = resultDecimal;
            this.remainderBinary = remainderBinary;
            this.remainderDecimal = remainderDecimal;
            this.carryOrBorrowLine = carryOrBorrowLine;
            this.alignedDisplay = alignedDisplay;
            this.steps = steps;
        }

        public Operation getOperation() {
            return operation;
        }

        public String getOperandABinary() {
            return operandABinary;
        }

        public String getOperandADecimal() {
            return operandADecimal;
        }

        public String getOperandBBinary() {
            return operandBBinary;
        }

        public String getOperandBDecimal() {
            return operandBDecimal;
        }

        public String getResultBinary() {
            return resultBinary;
        }

        public String getResultDecimal() {
            return resultDecimal;
        }

        public String getRemainderBinary() {
            return remainderBinary;
        }

        public String getRemainderDecimal() {
            return remainderDecimal;
        }

        public String getCarryOrBorrowLine() {
            return carryOrBorrowLine;
        }

        public String getAlignedDisplay() {
            return alignedDisplay;
        }

        public List<String> getSteps() {
            return steps;
        }
    }

    private BinaryArithmetic() {
    }

    /**
     * Validates and cleans a binary string.
     * Throws IllegalArgumentException on invalid input.
     */
    public static String sanitizeBinary(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Binary input cannot be null");
        }
        String trimmed = input.trim();
        if (trimmed.startsWith("+")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Binary input cannot be empty");
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException("Invalid binary digit: '" + c + "'. Only 0 and 1 are allowed.");
            }
        }
        // Normalize leading zeros, keeping at least one "0"
        int firstOne = 0;
        while (firstOne < trimmed.length() - 1 && trimmed.charAt(firstOne) == '0') {
            firstOne++;
        }
        return trimmed.substring(firstOne);
    }

    public static Result calculate(Operation operation, String binaryA, String binaryB) {
        String cleanA = sanitizeBinary(binaryA);
        String cleanB = sanitizeBinary(binaryB);
        BigInteger valA = new BigInteger(cleanA, 2);
        BigInteger valB = new BigInteger(cleanB, 2);

        switch (operation) {
            case ADD:
                return add(cleanA, cleanB, valA, valB);
            case SUBTRACT:
                return subtract(cleanA, cleanB, valA, valB);
            case MULTIPLY:
                return multiply(cleanA, cleanB, valA, valB);
            case DIVIDE:
                return divide(cleanA, cleanB, valA, valB);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }

    private static Result add(String cleanA, String cleanB, BigInteger valA, BigInteger valB) {
        BigInteger sum = valA.add(valB);
        String sumBin = sum.toString(2);
        int maxLen = Math.max(cleanA.length(), cleanB.length());
        String padA = padLeft(cleanA, maxLen);
        String padB = padLeft(cleanB, maxLen);

        List<String> stepList = new ArrayList<>();
        int carry = 0;
        char[] carryArray = new char[maxLen];

        for (int i = maxLen - 1; i >= 0; i--) {
            int bitA = padA.charAt(i) - '0';
            int bitB = padB.charAt(i) - '0';
            int colSum = bitA + bitB + carry;
            int sumBit = colSum % 2;
            int nextCarry = colSum / 2;
            int bitPos = maxLen - 1 - i;
            carryArray[i] = (carry == 1) ? '1' : ' ';
            stepList.add(0, String.format(
                    "Column 2^%d: %d + %d + carry %d = %d → sum bit %d, carry out %d",
                    bitPos, bitA, bitB, carry, colSum, sumBit, nextCarry));
            carry = nextCarry;
        }

        StringBuilder carryHeader = new StringBuilder();
        if (carry == 1) {
            carryHeader.append("1");
        } else {
            carryHeader.append(" ");
        }
        carryHeader.append(new String(carryArray));
        if (carry == 1) {
            stepList.add(0, String.format("Final carry-out generated at MSB: 1 (result width increases to %d bits)", sumBin.length()));
        }

        int width = Math.max(sumBin.length(), carryHeader.length()) + 2;
        StringBuilder aligned = new StringBuilder();
        aligned.append("Carry:   ").append(padLeft(carryHeader.toString(), width)).append("\n");
        aligned.append("         ").append(padLeft(cleanA, width)).append("  (").append(valA).append(")\n");
        aligned.append("     +   ").append(padLeft(cleanB, width)).append("  (").append(valB).append(")\n");
        aligned.append("     ").append(repeatChar('-', width + 2)).append("\n");
        aligned.append("         ").append(padLeft(sumBin, width)).append("  (").append(sum).append(")");

        return new Result(
                Operation.ADD,
                cleanA, valA.toString(10),
                cleanB, valB.toString(10),
                sumBin, sum.toString(10),
                null, null,
                carryHeader.toString().trim(),
                aligned.toString(),
                stepList
        );
    }

    private static Result subtract(String cleanA, String cleanB, BigInteger valA, BigInteger valB) {
        BigInteger diff = valA.subtract(valB);
        boolean isNegative = diff.signum() < 0;
        List<String> stepList = new ArrayList<>();

        if (!isNegative) {
            int maxLen = Math.max(cleanA.length(), cleanB.length());
            String padA = padLeft(cleanA, maxLen);
            String padB = padLeft(cleanB, maxLen);

            int borrow = 0;
            char[] borrowArray = new char[maxLen];

            for (int i = maxLen - 1; i >= 0; i--) {
                int bitA = padA.charAt(i) - '0';
                int bitB = padB.charAt(i) - '0';
                int bitPos = maxLen - 1 - i;
                borrowArray[i] = (borrow == 1) ? '1' : ' ';

                int nextBorrow;
                int diffBit;
                if (bitA - borrow >= bitB) {
                    diffBit = (bitA - borrow) - bitB;
                    nextBorrow = 0;
                    stepList.add(0, String.format(
                            "Column 2^%d: bitA(%d) - borrowIn(%d) - bitB(%d) = %d → diff bit %d, borrow out 0",
                            bitPos, bitA, borrow, bitB, diffBit, diffBit));
                } else {
                    diffBit = (bitA - borrow + 2) - bitB;
                    nextBorrow = 1;
                    stepList.add(0, String.format(
                            "Column 2^%d: bitA(%d) - borrowIn(%d) < bitB(%d) → borrow 2 from next column: (%d+2)-%d = %d, diff bit %d, borrow out 1",
                            bitPos, bitA, borrow, bitB, bitA - borrow, bitB, diffBit, diffBit));
                }
                borrow = nextBorrow;
            }

            String borrowHeader = new String(borrowArray);
            String diffBin = diff.toString(2);
            int width = Math.max(maxLen, diffBin.length()) + 2;

            StringBuilder aligned = new StringBuilder();
            aligned.append("Borrow:  ").append(padLeft(borrowHeader, width)).append("\n");
            aligned.append("         ").append(padLeft(cleanA, width)).append("  (").append(valA).append(")\n");
            aligned.append("     −   ").append(padLeft(cleanB, width)).append("  (").append(valB).append(")\n");
            aligned.append("     ").append(repeatChar('-', width + 2)).append("\n");
            aligned.append("         ").append(padLeft(diffBin, width)).append("  (").append(diff).append(")");

            return new Result(
                    Operation.SUBTRACT,
                    cleanA, valA.toString(10),
                    cleanB, valB.toString(10),
                    diffBin, diff.toString(10),
                    null, null,
                    borrowHeader.trim(),
                    aligned.toString(),
                    stepList
            );
        } else {
            // Negative subtraction: A < B => -(B - A)
            BigInteger magnitude = valB.subtract(valA);
            String magBin = magnitude.toString(2);
            String resultBin = "-" + magBin;
            stepList.add(String.format("Operand A (%s = %d) is smaller than Operand B (%s = %d).", cleanA, valA, cleanB, valB));
            stepList.add(String.format("Result is negative: −(B − A) = −(%d − %d) = −%d (in binary: −%s).", valB, valA, magnitude, magBin));
            stepList.add("In Two's Complement arithmetic, subtraction is computed as A + (~B + 1).");

            int width = Math.max(cleanB.length(), magBin.length()) + 2;
            StringBuilder aligned = new StringBuilder();
            aligned.append("Magnitude: ").append(padLeft(cleanB, width)).append("  (").append(valB).append(")\n");
            aligned.append("       −   ").append(padLeft(cleanA, width)).append("  (").append(valA).append(")\n");
            aligned.append("       ").append(repeatChar('-', width + 2)).append("\n");
            aligned.append("       −   ").append(padLeft(magBin, width)).append("  (−").append(magnitude).append(")");

            return new Result(
                    Operation.SUBTRACT,
                    cleanA, valA.toString(10),
                    cleanB, valB.toString(10),
                    resultBin, diff.toString(10),
                    null, null,
                    "N/A (A < B)",
                    aligned.toString(),
                    stepList
            );
        }
    }

    private static Result multiply(String cleanA, String cleanB, BigInteger valA, BigInteger valB) {
        BigInteger product = valA.multiply(valB);
        String prodBin = product.toString(2);
        List<String> stepList = new ArrayList<>();
        List<String> partialProducts = new ArrayList<>();

        int bLen = cleanB.length();
        for (int i = 0; i < bLen; i++) {
            int bitPos = i;
            char bit = cleanB.charAt(bLen - 1 - i);
            if (bit == '1') {
                String shifted = cleanA + repeatChar('0', bitPos);
                partialProducts.add(shifted);
                stepList.add(String.format("Bit %d of multiplier is 1: %s × 1 << %d = %s", bitPos, cleanA, bitPos, shifted));
            } else {
                String zeroTerm = repeatChar('0', cleanA.length() + bitPos);
                partialProducts.add(zeroTerm);
                stepList.add(String.format("Bit %d of multiplier is 0: %s × 0 << %d = %s", bitPos, cleanA, bitPos, zeroTerm));
            }
        }
        stepList.add(String.format("Summing all partial products gives: %s (%d in decimal)", prodBin, product));

        int width = Math.max(prodBin.length(), Math.max(cleanA.length(), cleanB.length())) + 2;
        StringBuilder aligned = new StringBuilder();
        aligned.append("         ").append(padLeft(cleanA, width)).append("  (").append(valA).append(")\n");
        aligned.append("     ×   ").append(padLeft(cleanB, width)).append("  (").append(valB).append(")\n");
        aligned.append("     ").append(repeatChar('-', width + 2)).append("\n");
        for (int i = 0; i < partialProducts.size(); i++) {
            aligned.append("         ").append(padLeft(partialProducts.get(i), width)).append("\n");
        }
        aligned.append("     ").append(repeatChar('=', width + 2)).append("\n");
        aligned.append("         ").append(padLeft(prodBin, width)).append("  (").append(product).append(")");

        return new Result(
                Operation.MULTIPLY,
                cleanA, valA.toString(10),
                cleanB, valB.toString(10),
                prodBin, product.toString(10),
                null, null,
                null,
                aligned.toString(),
                stepList
        );
    }

    private static Result divide(String cleanA, String cleanB, BigInteger valA, BigInteger valB) {
        if (valB.signum() == 0) {
            throw new ArithmeticException("Division by zero is undefined.");
        }

        BigInteger quotient = valA.divide(valB);
        BigInteger remainder = valA.remainder(valB);
        String quotBin = quotient.toString(2);
        String remBin = remainder.toString(2);

        List<String> stepList = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        StringBuilder buildingQuotient = new StringBuilder();

        for (int i = 0; i < cleanA.length(); i++) {
            char nextBit = cleanA.charAt(i);
            current.append(nextBit);
            BigInteger curVal = new BigInteger(current.toString(), 2);
            if (curVal.compareTo(valB) >= 0) {
                buildingQuotient.append('1');
                BigInteger sub = curVal.subtract(valB);
                stepList.add(String.format("Step %d: Bring down '%c', accumulator = %s (%d) ≥ divisor (%d) → quotient bit 1, remainder = %s (%d)",
                        i + 1, nextBit, current, curVal, valB, sub.toString(2), sub));
                current = new StringBuilder(sub.signum() == 0 ? "" : sub.toString(2));
            } else {
                buildingQuotient.append('0');
                stepList.add(String.format("Step %d: Bring down '%c', accumulator = %s (%d) < divisor (%d) → quotient bit 0",
                        i + 1, nextBit, current, curVal, valB));
            }
        }

        stepList.add(String.format("Final Quotient: %s (%d), Remainder: %s (%d)", quotBin, quotient, remBin, remainder));
        stepList.add(String.format("Verification: Dividend (%d) = Divisor (%d) × Quotient (%d) + Remainder (%d)",
                valA, valB, quotient, remainder));

        StringBuilder aligned = new StringBuilder();
        aligned.append("Dividend:  ").append(cleanA).append("  (").append(valA).append(")\n");
        aligned.append("Divisor:   ").append(cleanB).append("  (").append(valB).append(")\n");
        aligned.append("------------------------------------\n");
        aligned.append("Quotient:  ").append(quotBin).append("  (").append(quotient).append(")\n");
        aligned.append("Remainder: ").append(remBin).append("  (").append(remainder).append(")");

        return new Result(
                Operation.DIVIDE,
                cleanA, valA.toString(10),
                cleanB, valB.toString(10),
                quotBin, quotient.toString(10),
                remBin, remainder.toString(10),
                null,
                aligned.toString(),
                stepList
        );
    }

    private static String padLeft(String s, int length) {
        if (s.length() >= length) {
            return s;
        }
        return repeatChar(' ', length - s.length()) + s;
    }

    private static String repeatChar(char c, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
