package com.example.cscalc.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Safe parser, evaluator, truth-table generator, and equivalent SOP minimizer. */
public final class BooleanExpression {
    public static final int MAX_VARIABLES = 6;
    public static final int MAX_EXPRESSION_LENGTH = 2048;
    public static final int MAX_NESTING_DEPTH = 64;

    private final String expression;
    private final Node root;
    private final List<String> variables;

    private BooleanExpression(String expression, Node root, List<String> variables) {
        this.expression = expression;
        this.root = root;
        this.variables = Collections.unmodifiableList(new ArrayList<>(variables));
    }

    public static BooleanExpression parse(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new BooleanExpressionException("Enter a Boolean expression.");
        }
        if (expression.length() > MAX_EXPRESSION_LENGTH) {
            throw new BooleanExpressionException("Expression is too long.");
        }
        Parser parser = new Parser(expression);
        Node root = parser.parse();
        List<String> variables = new ArrayList<>(parser.variables);
        if (variables.size() > MAX_VARIABLES) {
            throw new BooleanExpressionException("Use at most " + MAX_VARIABLES
                    + " variables to generate a truth table.");
        }
        return new BooleanExpression(expression.trim(), root, variables);
    }

    public String getExpression() {
        return expression;
    }

    public List<String> getVariables() {
        return variables;
    }

    public boolean evaluate(Map<String, Boolean> assignment) {
        if (assignment == null) {
            throw new BooleanExpressionException("Provide an input value for each variable.");
        }
        Map<String, Boolean> normalized = new HashMap<>();
        for (String variable : variables) {
            Boolean value = assignment.get(variable);
            if (value == null) {
                value = assignment.get(variable.toLowerCase(java.util.Locale.ROOT));
            }
            if (value == null) {
                throw new BooleanExpressionException("Choose a value for " + variable + ".");
            }
            normalized.put(variable, value);
        }
        return root.evaluate(normalized);
    }

    public List<TruthRow> truthTable() {
        int rowCount = 1 << variables.size();
        List<TruthRow> rows = new ArrayList<>(rowCount);
        for (int row = 0; row < rowCount; row++) {
            Map<String, Boolean> assignment = assignmentFor(row);
            rows.add(new TruthRow(assignment, root.evaluate(assignment)));
        }
        return Collections.unmodifiableList(rows);
    }

    public Simplification simplify() {
        List<TruthRow> rows = truthTable();
        List<Integer> minterms = new ArrayList<>();
        for (int row = 0; row < rows.size(); row++) {
            if (rows.get(row).getOutput()) {
                minterms.add(row);
            }
        }
        if (minterms.isEmpty()) {
            return new Simplification("0", Collections.singletonList(
                    "The expression is false for every input, so it simplifies to 0."));
        }
        if (minterms.size() == rows.size()) {
            return new Simplification("1", Collections.singletonList(
                    "The expression is true for every input, so it simplifies to 1."));
        }
        if (variables.isEmpty()) {
            return new Simplification(rows.get(0).getOutput() ? "1" : "0",
                    Collections.<String>emptyList());
        }

        List<Cube> primes = findPrimeImplicants(minterms);
        List<Cube> selected = selectCover(primes, minterms);
        String result = formatSop(selected);
        List<String> identities = new ArrayList<>();
        identities.add("Truth-table minterms that produce 1 are combined only when they differ "
                + "in one variable.");
        identities.add("Each combination applies X·Y + X·¬Y = X (complement law), "
                + "repeated until no more terms combine.");
        identities.add("The resulting sum-of-products expression is equivalent for every "
                + "input assignment.");
        if (result.equals(expression)) {
            identities.add("The original expression is already in the generated simplified form.");
        }
        return new Simplification(result, identities);
    }

    private Map<String, Boolean> assignmentFor(int row) {
        Map<String, Boolean> assignment = new LinkedHashMap<>();
        for (int index = 0; index < variables.size(); index++) {
            int bitPosition = variables.size() - index - 1;
            assignment.put(variables.get(index), (row & (1 << bitPosition)) != 0);
        }
        return assignment;
    }

    private List<Cube> findPrimeImplicants(List<Integer> minterms) {
        int fullMask = (1 << variables.size()) - 1;
        List<Cube> current = new ArrayList<>();
        for (Integer minterm : minterms) {
            current.add(new Cube(minterm, fullMask));
        }
        Set<Cube> primes = new HashSet<>();
        while (!current.isEmpty()) {
            boolean[] combined = new boolean[current.size()];
            Set<Cube> nextSet = new HashSet<>();
            for (int leftIndex = 0; leftIndex < current.size(); leftIndex++) {
                for (int rightIndex = leftIndex + 1; rightIndex < current.size(); rightIndex++) {
                    Cube left = current.get(leftIndex);
                    Cube right = current.get(rightIndex);
                    Cube combinedCube = left.combine(right);
                    if (combinedCube != null) {
                        combined[leftIndex] = true;
                        combined[rightIndex] = true;
                        nextSet.add(combinedCube);
                    }
                }
            }
            for (int index = 0; index < current.size(); index++) {
                if (!combined[index]) {
                    primes.add(current.get(index));
                }
            }
            current = new ArrayList<>(nextSet);
        }
        List<Cube> result = new ArrayList<>(primes);
        result.sort(Comparator.comparingInt(Cube::literalCount).reversed()
                .thenComparingInt(Cube::getValue));
        return result;
    }

    private List<Cube> selectCover(List<Cube> primes, List<Integer> minterms) {
        Set<Integer> uncovered = new HashSet<>(minterms);
        List<Cube> selected = new ArrayList<>();
        while (!uncovered.isEmpty()) {
            Cube best = null;
            int bestCoverage = 0;
            for (Cube prime : primes) {
                int coverage = 0;
                for (Integer minterm : uncovered) {
                    if (prime.matches(minterm)) {
                        coverage++;
                    }
                }
                if (coverage > bestCoverage
                        || (coverage == bestCoverage && coverage > 0
                        && (best == null || prime.literalCount() < best.literalCount()))) {
                    best = prime;
                    bestCoverage = coverage;
                }
            }
            if (best == null) {
                throw new IllegalStateException("Truth-table cover is incomplete");
            }
            selected.add(best);
            for (Integer minterm : minterms) {
                if (best.matches(minterm)) {
                    uncovered.remove(minterm);
                }
            }
        }
        return selected;
    }

    private String formatSop(List<Cube> cubes) {
        List<String> terms = new ArrayList<>();
        for (Cube cube : cubes) {
            List<String> literals = new ArrayList<>();
            for (int index = 0; index < variables.size(); index++) {
                int bitPosition = variables.size() - index - 1;
                int bitMask = 1 << bitPosition;
                if ((cube.mask & bitMask) != 0) {
                    literals.add((cube.value & bitMask) != 0
                            ? variables.get(index) : "!" + variables.get(index));
                }
            }
            if (literals.isEmpty()) {
                return "1";
            }
            terms.add(literals.size() == 1 ? literals.get(0)
                    : "(" + join(literals, " & ") + ")");
        }
        return join(terms, " | ");
    }

    private String join(List<String> parts, String separator) {
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(part);
        }
        return result.toString();
    }

    private interface Node {
        boolean evaluate(Map<String, Boolean> variables);
    }

    private static final class VariableNode implements Node {
        private final String name;

        VariableNode(String name) {
            this.name = name;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> variables) {
            Boolean value = variables.get(name);
            if (value == null) {
                throw new BooleanExpressionException("Choose a value for " + name + ".");
            }
            return value;
        }
    }

    private static final class ConstantNode implements Node {
        private final boolean value;

        ConstantNode(boolean value) {
            this.value = value;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> variables) {
            return value;
        }
    }

    private static final class NotNode implements Node {
        private final Node child;

        NotNode(Node child) {
            this.child = child;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> variables) {
            return !child.evaluate(variables);
        }
    }

    private static final class BinaryNode implements Node {
        private final Node left;
        private final Node right;
        private final boolean isAnd;

        BinaryNode(Node left, Node right, boolean isAnd) {
            this.left = left;
            this.right = right;
            this.isAnd = isAnd;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> variables) {
            return isAnd
                    ? left.evaluate(variables) && right.evaluate(variables)
                    : left.evaluate(variables) || right.evaluate(variables);
        }
    }

    public static final class TruthRow {
        private final Map<String, Boolean> assignment;
        private final boolean output;

        private TruthRow(Map<String, Boolean> assignment, boolean output) {
            this.assignment = Collections.unmodifiableMap(new LinkedHashMap<>(assignment));
            this.output = output;
        }

        public Map<String, Boolean> getAssignment() {
            return assignment;
        }

        public boolean getOutput() {
            return output;
        }
    }

    public static final class Simplification {
        private final String expression;
        private final List<String> explanations;

        private Simplification(String expression, List<String> explanations) {
            this.expression = expression;
            this.explanations = Collections.unmodifiableList(new ArrayList<>(explanations));
        }

        public String getExpression() {
            return expression;
        }

        public List<String> getExplanations() {
            return explanations;
        }
    }

    public static final class BooleanExpressionException extends IllegalArgumentException {
        BooleanExpressionException(String message) {
            super(message);
        }
    }

    private static final class Cube {
        private final int value;
        private final int mask;

        Cube(int value, int mask) {
            this.value = value & mask;
            this.mask = mask;
        }

        Cube combine(Cube other) {
            if (mask != other.mask) {
                return null;
            }
            int difference = (value ^ other.value) & mask;
            if (Integer.bitCount(difference) != 1) {
                return null;
            }
            return new Cube(value & ~difference, mask & ~difference);
        }

        boolean matches(int minterm) {
            return (minterm & mask) == value;
        }

        int literalCount() {
            return Integer.bitCount(mask);
        }

        int getValue() {
            return value;
        }
    }

    private static final class Parser {
        private final String input;
        private final Set<String> variables = new TreeSet<>();
        private int position;
        private int nestingDepth;

        Parser(String input) {
            this.input = input;
        }

        Node parse() {
            Node node = parseOr();
            skipWhitespace();
            if (position != input.length()) {
                throw error("Unexpected token.");
            }
            return node;
        }

        private Node parseOr() {
            Node node = parseAnd();
            while (true) {
                skipWhitespace();
                if (match('|') || matchWord("OR")) {
                    node = new BinaryNode(node, parseAnd(), false);
                } else {
                    return node;
                }
            }
        }

        private Node parseAnd() {
            Node node = parseUnary();
            while (true) {
                skipWhitespace();
                if (match('&') || matchWord("AND")) {
                    node = new BinaryNode(node, parseUnary(), true);
                } else {
                    return node;
                }
            }
        }

        private Node parseUnary() {
            skipWhitespace();
            if (match('!') || match('~')) {
                return new NotNode(parseUnary());
            }
            if (matchWord("NOT")) {
                return new NotNode(parseUnary());
            }
            return parsePrimary();
        }

        private Node parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                nestingDepth++;
                if (nestingDepth > MAX_NESTING_DEPTH) {
                    throw error("Parentheses are nested too deeply.");
                }
                try {
                    Node node = parseOr();
                    skipWhitespace();
                    if (!match(')')) {
                        throw error("Missing closing parenthesis.");
                    }
                    return node;
                } finally {
                    nestingDepth--;
                }
            }
            if (position >= input.length()) {
                throw error("Expected a variable or opening parenthesis.");
            }
            char character = input.charAt(position);
            if (character == '0' || character == '1') {
                position++;
                return new ConstantNode(character == '1');
            }
            if (!Character.isLetter(character) && character != '_') {
                throw error("Expected a variable, 0, 1, or opening parenthesis.");
            }
            int start = position++;
            while (position < input.length()) {
                char next = input.charAt(position);
                if (!Character.isLetterOrDigit(next) && next != '_') {
                    break;
                }
                position++;
            }
            String name = input.substring(start, position).toUpperCase(java.util.Locale.ROOT);
            if ("AND".equals(name) || "OR".equals(name) || "NOT".equals(name)) {
                throw error("Operator '" + name + "' needs an operand.");
            }
            variables.add(name);
            return new VariableNode(name);
        }

        private boolean matchWord(String word) {
            skipWhitespace();
            if (position + word.length() > input.length()
                    || !input.regionMatches(true, position, word, 0, word.length())) {
                return false;
            }
            int end = position + word.length();
            if (position > 0 && isWordCharacter(input.charAt(position - 1))) {
                return false;
            }
            if (end < input.length() && isWordCharacter(input.charAt(end))) {
                return false;
            }
            position = end;
            return true;
        }

        private boolean isWordCharacter(char value) {
            return Character.isLetterOrDigit(value) || value == '_';
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

        private BooleanExpressionException error(String message) {
            return new BooleanExpressionException(message + " (position "
                    + (position + 1) + ")");
        }
    }
}
