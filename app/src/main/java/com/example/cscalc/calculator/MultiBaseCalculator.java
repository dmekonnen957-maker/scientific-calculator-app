package com.example.cscalc.calculator;

import com.example.cscalc.converter.NumberBase;
import com.example.cscalc.utils.InputValidator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Exact integer expression evaluator for the four supported number bases. */
public final class MultiBaseCalculator {
    public static final int MAX_EXPRESSION_LENGTH = 8192;
    public static final int MAX_NESTING_DEPTH = 128;

    private MultiBaseCalculator() {
    }

    public static Calculation evaluate(String expression, NumberBase base) {
        if (base == null) {
            throw new IllegalArgumentException("Choose a number base.");
        }
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionException("Enter an expression.");
        }
        if (expression.length() > MAX_EXPRESSION_LENGTH) {
            throw new ExpressionException("Expression is too long.");
        }
        Parser parser = new Parser(expression, base);
        BigInteger result = parser.parse();
        return new Calculation(expression.trim(), result, base, parser.getSteps());
    }

    public static final class Calculation {
        private final String expression;
        private final BigInteger result;
        private final NumberBase base;
        private final List<String> steps;

        private Calculation(String expression, BigInteger result, NumberBase base,
                            List<String> steps) {
            this.expression = expression;
            this.result = result;
            this.base = base;
            this.steps = new ArrayList<>(steps);
        }

        public String getExpression() {
            return expression;
        }

        public BigInteger getResult() {
            return result;
        }

        public String getResultInBase() {
            return result.toString(base.getRadix()).toUpperCase(java.util.Locale.ROOT);
        }

        public String getDecimalResult() {
            return result.toString();
        }

        public List<String> getSteps() {
            return new ArrayList<>(steps);
        }

        public String getExplanation() {
            StringBuilder explanation = new StringBuilder();
            explanation.append("Mode: base ").append(base.getRadix())
                    .append(" (").append(base.getShortName()).append(")\n");
            if (steps.isEmpty()) {
                explanation.append("The expression is a single integer value.");
            } else {
                explanation.append("Evaluate parentheses first, then multiplication and division, ")
                        .append("then addition and subtraction. Operators of equal precedence ")
                        .append("are evaluated left to right.\n");
                for (int index = 0; index < steps.size(); index++) {
                    explanation.append(index + 1).append(". ").append(steps.get(index)).append('\n');
                }
            }
            explanation.append("Result: ").append(getResultInBase())
                    .append(" (base ").append(base.getRadix()).append(") = ")
                    .append(getDecimalResult()).append(" (decimal).");
            if (steps.stream().anyMatch(step -> step.contains(" ÷ "))) {
                explanation.append("\nInteger division truncates toward zero; remainders are discarded.");
            }
            return explanation.toString();
        }
    }

    public static final class ExpressionException extends IllegalArgumentException {
        public ExpressionException(String message) {
            super(message);
        }
    }

    private static final class Parser {
        private final String input;
        private final NumberBase base;
        private final List<String> steps = new ArrayList<>();
        private int position;
        private int nestingDepth;

        Parser(String input, NumberBase base) {
            this.input = input;
            this.base = base;
        }

        BigInteger parse() {
            validateDigits();
            BigInteger value = parseExpression();
            skipWhitespace();
            if (position != input.length()) {
                throw error("Unexpected character '" + input.charAt(position) + "'.");
            }
            return value;
        }

        private void validateDigits() {
            for (int index = 0; index < input.length(); index++) {
                char character = input.charAt(index);
                if (Character.isLetterOrDigit(character)
                        && !InputValidator.isDigitAllowed(character, base)) {
                    position = index;
                    throw error("Digit '" + character + "' is not valid in base "
                            + base.getRadix() + ".");
                }
            }
        }

        List<String> getSteps() {
            return steps;
        }

        private BigInteger parseExpression() {
            BigInteger value = parseTerm();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    value = apply(value, parseTerm(), '+');
                } else if (match('-')) {
                    value = apply(value, parseTerm(), '-');
                } else {
                    return value;
                }
            }
        }

        private BigInteger parseTerm() {
            BigInteger value = parseUnary();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    value = apply(value, parseUnary(), '*');
                } else if (match('/')) {
                    value = apply(value, parseUnary(), '/');
                } else {
                    return value;
                }
            }
        }

        private BigInteger parseUnary() {
            skipWhitespace();
            boolean negative = false;
            while (true) {
                if (match('+')) {
                    skipWhitespace();
                } else if (match('-')) {
                    negative = !negative;
                    skipWhitespace();
                } else {
                    break;
                }
            }
            BigInteger value = parsePrimary();
            return negative ? value.negate() : value;
        }

        private BigInteger parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                nestingDepth++;
                if (nestingDepth > MAX_NESTING_DEPTH) {
                    throw error("Expression nesting is too deep.");
                }
                try {
                    BigInteger value = parseExpression();
                    skipWhitespace();
                    if (!match(')')) {
                        throw error("Missing closing parenthesis.");
                    }
                    return value;
                } finally {
                    nestingDepth--;
                }
            }

            int start = position;
            while (position < input.length()
                    && InputValidator.isDigitAllowed(input.charAt(position), base)) {
                position++;
            }
            if (start == position) {
                if (position >= input.length()) {
                    throw error("Expected a number.");
                }
                char character = input.charAt(position);
                if (Character.isLetterOrDigit(character)) {
                    throw error("Digit '" + character + "' is not valid in base "
                            + base.getRadix() + ".");
                }
                throw error("Expected a number or opening parenthesis.");
            }

            try {
                return new BigInteger(input.substring(start, position), base.getRadix());
            } catch (NumberFormatException exception) {
                throw error("Invalid integer.");
            }
        }

        private BigInteger apply(BigInteger left, BigInteger right, char operator) {
            BigInteger result;
            switch (operator) {
                case '+':
                    result = left.add(right);
                    break;
                case '-':
                    result = left.subtract(right);
                    break;
                case '*':
                    result = left.multiply(right);
                    break;
                case '/':
                    if (right.signum() == 0) {
                        throw error("Division by zero is undefined.");
                    }
                    result = left.divide(right);
                    break;
                default:
                    throw new IllegalStateException("Unknown arithmetic operator");
            }
            steps.add(format(left) + " " + operatorText(operator) + " " + format(right)
                    + " = " + format(result));
            return result;
        }

        private String format(BigInteger value) {
            return value.toString(base.getRadix()).toUpperCase(java.util.Locale.ROOT);
        }

        private String operatorText(char operator) {
            switch (operator) {
                case '+':
                    return "+";
                case '-':
                    return "−";
                case '*':
                    return "×";
                case '/':
                    return "÷";
                default:
                    throw new IllegalStateException("Unknown arithmetic operator");
            }
        }

        private boolean match(char expected) {
            if (position < input.length() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
                position++;
            }
        }

        private ExpressionException error(String message) {
            return new ExpressionException(message + " (position " + (position + 1) + ")");
        }
    }
}
