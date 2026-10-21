package com.example.cscalc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.example.cscalc.calculator.BooleanExpression;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BooleanAlgebraActivity extends ToolkitActivity {
    private EditText expressionInput;
    private LinearLayout assignmentsContainer;
    private LinearLayout truthTableContainer;
    private TextView evaluatedOutput;
    private TextView errorView;
    private TextView simplificationView;
    private LinearLayout simplificationExplanations;
    private BooleanExpression parsedExpression;
    private final Map<String, SwitchCompat> assignmentSwitches = new LinkedHashMap<>();
    private boolean loadingSample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boolean_algebra);
        SystemBarInsets.apply(findViewById(R.id.booleanRoot));

        expressionInput = findViewById(R.id.booleanExpressionInput);
        assignmentsContainer = findViewById(R.id.booleanAssignments);
        truthTableContainer = findViewById(R.id.booleanTruthTable);
        evaluatedOutput = findViewById(R.id.booleanEvaluatedOutput);
        errorView = findViewById(R.id.booleanError);
        simplificationView = findViewById(R.id.booleanSimplifiedExpression);
        simplificationExplanations = findViewById(R.id.booleanSimplificationExplanations);
        expressionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                parsedExpression = null;
                evaluatedOutput.setText(R.string.result_placeholder);
                evaluatedOutput.setBackgroundResource(R.drawable.bg_tool_card);
                truthTableContainer.removeAllViews();
                simplificationView.setText(R.string.boolean_simplified_prompt);
                simplificationExplanations.removeAllViews();
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        findViewById(R.id.booleanBack).setOnClickListener(view -> finish());
        findViewById(R.id.booleanSampleButton).setOnClickListener(view -> {
            loadingSample = true;
            expressionInput.setText("A AND (NOT B OR C)");
            loadingSample = false;
            prepareExpression();
        });
        findViewById(R.id.booleanPrepareButton).setOnClickListener(view -> prepareExpression());
        findViewById(R.id.booleanEvaluateButton).setOnClickListener(view -> evaluateAssignment());
        findViewById(R.id.booleanTruthTableButton).setOnClickListener(view -> generateTruthTable());
        findViewById(R.id.booleanSimplifyButton).setOnClickListener(view -> simplify());
        findViewById(R.id.booleanClearButton).setOnClickListener(view -> clear());
        expressionInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && !loadingSample && expressionInput.length() > 0) {
                prepareExpression();
            }
        });
        handleReuseIntent();
    }

    private void handleReuseIntent() {
        if (getIntent() != null && getIntent().hasExtra("REUSE_EXPRESSION")) {
            String expr = getIntent().getStringExtra("REUSE_EXPRESSION");
            if (expr != null && !expr.isEmpty()) {
                expressionInput.setText(expr);
                prepareExpression();
            }
        }
    }

    private boolean prepareExpression() {
        try {
            parsedExpression = BooleanExpression.parse(expressionInput.getText().toString());
            errorView.setVisibility(View.GONE);
            renderAssignmentSwitches(parsedExpression.getVariables());
            return true;
        } catch (BooleanExpression.BooleanExpressionException exception) {
            parsedExpression = null;
            assignmentsContainer.removeAllViews();
            truthTableContainer.removeAllViews();
            showError(exception.getMessage());
            return false;
        }
    }

    private void renderAssignmentSwitches(List<String> variables) {
        assignmentsContainer.removeAllViews();
        assignmentSwitches.clear();
        if (variables.isEmpty()) {
            TextView constantNote = new TextView(this);
            constantNote.setText(R.string.boolean_constant_expression);
            constantNote.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            assignmentsContainer.addView(constantNote);
            return;
        }
        for (String variable : variables) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView label = new TextView(this);
            label.setText(variable);
            label.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            label.setTextSize(16);
            label.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(label, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            SwitchCompat switchView = new SwitchCompat(this);
            switchView.setTextOn("1");
            switchView.setTextOff("0");
            switchView.setShowText(true);
            switchView.setContentDescription(getString(R.string.boolean_variable_accessibility,
                    variable));
            row.addView(switchView);
            assignmentsContainer.addView(row);
            assignmentSwitches.put(variable, switchView);
        }
    }

    private void evaluateAssignment() {
        if (parsedExpression == null && !prepareExpression()) {
            return;
        }
        Map<String, Boolean> assignment = currentAssignment();
        try {
            boolean output = parsedExpression.evaluate(assignment);
            evaluatedOutput.setText(output ? "1" : "0");
            evaluatedOutput.setBackgroundResource(output
                    ? R.drawable.bg_logic_output_set : R.drawable.bg_logic_output_clear);
            StringBuilder record = new StringBuilder("BOOLEAN ")
                    .append(parsedExpression.getExpression()).append(" [");
            for (String variable : parsedExpression.getVariables()) {
                if (record.charAt(record.length() - 1) != '[') {
                    record.append(", ");
                }
                record.append(variable).append('=').append(assignment.get(variable) ? '1' : '0');
            }
            record.append("] = ").append(output ? '1' : '0');
            CalculationHistory.record(this,
                    com.example.cscalc.history.HistoryRepository.MODULE_BOOLEAN,
                    "Evaluate Expression",
                    parsedExpression.getExpression(),
                    output ? "1" : "0",
                    "Assignments: " + assignment.toString(),
                    record.toString());
            errorView.setVisibility(View.GONE);
            Toast.makeText(this, R.string.boolean_evaluation_saved, Toast.LENGTH_SHORT).show();
        } catch (BooleanExpression.BooleanExpressionException exception) {
            showError(exception.getMessage());
        }
    }

    private void generateTruthTable() {
        if (parsedExpression == null && !prepareExpression()) {
            return;
        }
        truthTableContainer.removeAllViews();
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        for (String variable : parsedExpression.getVariables()) {
            addTruthCell(header, variable, true);
        }
        addTruthCell(header, "Y", true);
        truthTableContainer.addView(header);
        addDivider(truthTableContainer);

        for (BooleanExpression.TruthRow row : parsedExpression.truthTable()) {
            LinearLayout tableRow = new LinearLayout(this);
            tableRow.setOrientation(LinearLayout.HORIZONTAL);
            for (String variable : parsedExpression.getVariables()) {
                addTruthCell(tableRow, row.getAssignment().get(variable) ? "1" : "0", false);
            }
            addTruthCell(tableRow, row.getOutput() ? "1" : "0", false);
            truthTableContainer.addView(tableRow);
        }
    }

    private void simplify() {
        if (parsedExpression == null && !prepareExpression()) {
            return;
        }
        BooleanExpression.Simplification simplification = parsedExpression.simplify();
        simplificationView.setText(simplification.getExpression());
        simplificationExplanations.removeAllViews();
        for (String explanation : simplification.getExplanations()) {
            TextView item = new TextView(this);
            item.setText("• " + explanation);
            item.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            item.setTextSize(13);
            item.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                    0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall));
            simplificationExplanations.addView(item);
        }
        CalculationHistory.record(this,
                com.example.cscalc.history.HistoryRepository.MODULE_BOOLEAN,
                "Simplify Expression",
                parsedExpression.getExpression(),
                simplification.getExpression(),
                "Equivalent SOP",
                "Equivalent SOP: " + simplification.getExpression());
    }

    private Map<String, Boolean> currentAssignment() {
        Map<String, Boolean> assignment = new LinkedHashMap<>();
        for (Map.Entry<String, SwitchCompat> entry : assignmentSwitches.entrySet()) {
            assignment.put(entry.getKey(), entry.getValue().isChecked());
        }
        return assignment;
    }

    private void addTruthCell(LinearLayout row, String text, boolean header) {
        TextView cell = new TextView(this);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        cell.setGravity(android.view.Gravity.CENTER);
        cell.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_small),
                0, getResources().getDimensionPixelSize(R.dimen.spacing_small));
        cell.setText(text);
        cell.setTextColor(ContextCompat.getColor(this,
                header ? R.color.text_secondary : R.color.text_primary));
        cell.setTextSize(header ? 12 : 15);
        if (header) {
            cell.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        row.addView(cell);
    }

    private void addDivider(LinearLayout container) {
        View divider = new View(this);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.card_outline));
        container.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void clear() {
        expressionInput.setText("");
        parsedExpression = null;
        assignmentSwitches.clear();
        assignmentsContainer.removeAllViews();
        truthTableContainer.removeAllViews();
        simplificationExplanations.removeAllViews();
        simplificationView.setText(R.string.boolean_simplified_prompt);
        evaluatedOutput.setText(R.string.result_placeholder);
        evaluatedOutput.setBackgroundResource(R.drawable.bg_logic_output_clear);
        errorView.setVisibility(View.GONE);
    }
}
