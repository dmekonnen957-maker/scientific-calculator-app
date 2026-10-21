package com.example.cscalc;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cscalc.calculator.MultiBaseCalculator;
import com.example.cscalc.converter.NumberBase;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class MultiBaseCalculatorActivity extends ToolkitActivity {
    private static final String[] DIGITS = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"
    };

    private EditText expressionInput;
    private Spinner baseSpinner;
    private GridLayout keypad;
    private TextView resultView;
    private TextView decimalView;
    private TextView explanationView;
    private TextView errorView;
    private Button copyButton;
    private NumberBase currentBase = NumberBase.DECIMAL;
    private MultiBaseCalculator.Calculation lastCalculation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_base_calculator);
        SystemBarInsets.apply(findViewById(R.id.calculatorRoot));

        expressionInput = findViewById(R.id.expressionInput);
        baseSpinner = findViewById(R.id.calculatorBaseSpinner);
        keypad = findViewById(R.id.calculatorKeypad);
        resultView = findViewById(R.id.calculatorResult);
        decimalView = findViewById(R.id.calculatorDecimalResult);
        explanationView = findViewById(R.id.calculatorExplanation);
        errorView = findViewById(R.id.calculatorError);
        copyButton = findViewById(R.id.copyCalculationResult);

        expressionInput.setShowSoftInputOnFocus(false);
        expressionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                resetResult();
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        String[] baseNames = new String[NumberBase.values().length];
        for (int index = 0; index < NumberBase.values().length; index++) {
            NumberBase base = NumberBase.values()[index];
            baseNames[index] = base.getShortName() + "  ·  base " + base.getRadix();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, baseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        baseSpinner.setAdapter(adapter);

        baseSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            private int previousSelection = -1;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                NumberBase selected = NumberBase.values()[position];
                if (previousSelection != -1 && selected != currentBase) {
                    expressionInput.setText("");
                    resetResult();
                    showMessage(getString(R.string.calculator_mode_changed));
                }
                previousSelection = position;
                currentBase = selected;
                buildKeypad();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        findViewById(R.id.calculatorBack).setOnClickListener(view -> finish());
        findViewById(R.id.calculateButton).setOnClickListener(view -> calculate());
        findViewById(R.id.clearExpressionButton).setOnClickListener(view -> clearExpression());
        findViewById(R.id.backspaceExpressionButton).setOnClickListener(view -> backspace());
        copyButton.setOnClickListener(view -> copyResult());
        handleReuseIntent();
    }

    private void handleReuseIntent() {
        if (getIntent() != null && getIntent().hasExtra("REUSE_EXPRESSION")) {
            String expr = getIntent().getStringExtra("REUSE_EXPRESSION");
            String base = getIntent().getStringExtra("REUSE_BASE");
            if (base != null) {
                for (int i = 0; i < NumberBase.values().length; i++) {
                    if (NumberBase.values()[i].getShortName().equalsIgnoreCase(base) ||
                            NumberBase.values()[i].name().equalsIgnoreCase(base)) {
                        baseSpinner.setSelection(i);
                        break;
                    }
                }
            }
            if (expr != null && !expr.isEmpty()) {
                expressionInput.setText(expr);
                calculate();
            }
        }
    }

    private void buildKeypad() {
        keypad.removeAllViews();
        List<String> keys = new ArrayList<>();
        int radix = currentBase.getRadix();
        for (int digit = radix - 1; digit >= 0; digit--) {
            keys.add(DIGITS[digit]);
        }
        keys.add("(");
        keys.add(")");
        keys.add("+");
        keys.add("−");
        keys.add("×");
        keys.add("÷");

        keypad.setColumnCount(4);
        keypad.setRowCount((keys.size() + 3) / 4);
        for (String key : keys) {
            Button button = new Button(this);
            button.setText(key);
            button.setTextSize(20);
            button.setAllCaps(false);
            button.setMinHeight(getResources().getDimensionPixelSize(R.dimen.calculator_key_height));
            button.setPadding(0, 0, 0, 0);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    isOperator(key) ? ContextCompat.getColor(this, R.color.calculator_operator)
                            : ContextCompat.getColor(this, R.color.surface)));
            button.setTextColor(ContextCompat.getColor(this, isOperator(key)
                    ? R.color.calculator_operator_text : R.color.text_primary));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(R.dimen.calculator_key_height);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            keypad.addView(button, params);
            button.setOnClickListener(view -> insertKey(key));
        }
    }

    private boolean isOperator(String key) {
        return "+".equals(key) || "−".equals(key) || "×".equals(key)
                || "÷".equals(key) || "(".equals(key) || ")".equals(key);
    }

    private void insertKey(String key) {
        String insertion = key;
        if ("−".equals(key)) {
            insertion = "-";
        } else if ("×".equals(key)) {
            insertion = "*";
        } else if ("÷".equals(key)) {
            insertion = "/";
        }
        int start = Math.max(expressionInput.getSelectionStart(), 0);
        int end = Math.max(expressionInput.getSelectionEnd(), 0);
        expressionInput.getText().replace(Math.min(start, end), Math.max(start, end), insertion);
        errorView.setVisibility(View.GONE);
    }

    private void calculate() {
        hideKeyboard();
        try {
            lastCalculation = MultiBaseCalculator.evaluate(
                    expressionInput.getText().toString(), currentBase);
            resultView.setText(lastCalculation.getResultInBase());
            decimalView.setText(getString(R.string.calculator_decimal_value,
                    lastCalculation.getDecimalResult()));
            explanationView.setText(lastCalculation.getExplanation());
            errorView.setVisibility(View.GONE);
            copyButton.setEnabled(true);
            CalculationHistory.record(this,
                    com.example.cscalc.history.HistoryRepository.MODULE_CALCULATOR,
                    "Expression Evaluation",
                    lastCalculation.getExpression(),
                    lastCalculation.getResultInBase() + " (DEC " + lastCalculation.getDecimalResult() + ")",
                    "Base: " + currentBase.getShortName(),
                    lastCalculation.getExplanation());
        } catch (MultiBaseCalculator.ExpressionException exception) {
            resetResult();
            errorView.setText(exception.getMessage());
            errorView.setVisibility(View.VISIBLE);
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearExpression() {
        expressionInput.setText("");
        resetResult();
        errorView.setVisibility(View.GONE);
    }

    private void backspace() {
        int start = expressionInput.getSelectionStart();
        int end = expressionInput.getSelectionEnd();
        if (start != end) {
            expressionInput.getText().delete(Math.min(start, end), Math.max(start, end));
        } else if (start > 0) {
            expressionInput.getText().delete(start - 1, start);
        }
        errorView.setVisibility(View.GONE);
    }

    private void resetResult() {
        lastCalculation = null;
        resultView.setText(R.string.result_placeholder);
        decimalView.setText(R.string.calculator_decimal_prompt);
        explanationView.setText(R.string.calculator_explanation_prompt);
        copyButton.setEnabled(false);
        copyButton.setAlpha(0.4f);
    }

    private void copyResult() {
        if (lastCalculation == null) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(
                getString(R.string.calculator_result_label), lastCalculation.getResultInBase()));
        Toast.makeText(this, R.string.result_copied, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
