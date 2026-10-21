package com.example.cscalc.calculator;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BooleanExpressionTest {
    @Test
    public void evaluatesNotAndAndOrWithExpectedPrecedence() {
        BooleanExpression expression = BooleanExpression.parse("NOT A AND B OR C");
        assertFalse(expression.evaluate(values(false, false, false)));
        assertTrue(expression.evaluate(values(false, true, false)));
        assertTrue(expression.evaluate(values(true, false, true)));
        assertFalse(expression.evaluate(values(true, true, false)));
    }

    @Test
    public void supportsSymbolicOperatorsAndParentheses() {
        BooleanExpression expression = BooleanExpression.parse("!(A & B) | C");
        assertTrue(expression.evaluate(values(false, true, false)));
        assertFalse(expression.evaluate(values(true, true, false)));
        assertTrue(expression.evaluate(values(true, true, true)));
    }

    @Test
    public void supportsConstantsAndCaseInsensitiveNames() {
        BooleanExpression expression = BooleanExpression.parse("ready AND 1");
        assertEquals(Collections.singletonList("READY"), expression.getVariables());
        assertTrue(expression.evaluate(Collections.singletonMap("ready", true)));
        assertFalse(expression.evaluate(Collections.singletonMap("READY", false)));
    }

    @Test
    public void generatesTruthTableForAllAssignments() {
        BooleanExpression expression = BooleanExpression.parse("A AND NOT B");
        assertEquals(4, expression.truthTable().size());
        assertFalse(expression.truthTable().get(0).getOutput());
        assertFalse(expression.truthTable().get(1).getOutput());
        assertTrue(expression.truthTable().get(2).getOutput());
        assertFalse(expression.truthTable().get(3).getOutput());
    }

    @Test
    public void simplifiesUsingEquivalentTruthTableMinterms() {
        assertEquivalentAfterSimplification("A AND B OR A AND NOT B", "A");
        assertEquivalentAfterSimplification("A OR NOT A", "1");
        assertEquivalentAfterSimplification("A AND NOT A", "0");
        assertEquivalentAfterSimplification("A AND (B OR C)", null);
    }

    @Test
    public void rejectsMissingOperandsUnexpectedTokensAndTooManyVariables() {
        assertInvalid("A AND");
        assertInvalid("(A OR B");
        assertInvalid("A B");
        assertInvalid("A AND $");
        assertInvalid("A AND B AND C AND D AND E AND F AND G");
    }

    @Test
    public void rejectsMissingAssignment() {
        try {
            BooleanExpression.parse("A OR B").evaluate(Collections.singletonMap("A", true));
            fail("Expected missing variable assignment to be rejected");
        } catch (BooleanExpression.BooleanExpressionException exception) {
            assertTrue(exception.getMessage().contains("Choose a value for B"));
        }
    }

    private void assertEquivalentAfterSimplification(String original, String expected) {
        BooleanExpression expression = BooleanExpression.parse(original);
        BooleanExpression.Simplification simplified = expression.simplify();
        if (expected != null) {
            assertEquals(expected, simplified.getExpression());
        }
        BooleanExpression simplifiedExpression = BooleanExpression.parse(simplified.getExpression());
        for (BooleanExpression.TruthRow row : expression.truthTable()) {
            assertEquals(row.getOutput(), simplifiedExpression.evaluate(row.getAssignment()));
        }
    }

    private void assertInvalid(String expression) {
        try {
            BooleanExpression.parse(expression);
            fail("Expected invalid expression to be rejected: " + expression);
        } catch (BooleanExpression.BooleanExpressionException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
    }

    private Map<String, Boolean> values(boolean a, boolean b, boolean c) {
        Map<String, Boolean> values = new HashMap<>();
        values.put("A", a);
        values.put("B", b);
        values.put("C", c);
        return values;
    }
}
