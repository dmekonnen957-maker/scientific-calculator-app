package com.example.cscalc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cscalc.calculator.BinaryArithmetic;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.math.BigInteger;

public class BinaryArithmeticActivity extends ToolkitActivity {

    private Spinner opSpinner;
    private EditText operandAInput;
    private EditText operandBInput;
    private TextView operandADecimalPreview;
    private TextView operandBDecimalPreview;
    private TextView errorView;
    private TextView resultText;
    private TextView decimalResultText;
    private TextView remainderText;
    private TextView alignedDisplay;
    private LinearLayout stepsContainer;
    private TextView stepsEmpty;
    private View saveButton;
    private BinaryArithmetic.Result lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_arithmetic);
        SystemBarInsets.apply(findViewById(R.id.binaryRoot));

        opSpinner = findViewById(R.id.binaryOpSpinner);
        operandAInput = findViewById(R.id.binaryOperandA);
        operandBInput = findViewById(R.id.binaryOperandB);
        operandADecimalPreview = findViewById(R.id.operandADecimalPreview);
        operandBDecimalPreview = findViewById(R.id.operandBDecimalPreview);
        errorView = findViewById(R.id.binaryError);
        resultText = findViewById(R.id.binaryResultText);
        decimalResultText = findViewById(R.id.binaryDecimalResultText);
        remainderText = findViewById(R.id.binaryRemainderText);
        alignedDisplay = findViewById(R.id.binaryAlignedDisplay);
        stepsContainer = findViewById(R.id.binaryStepsContainer);
        stepsEmpty = findViewById(R.id.binaryStepsEmpty);
        saveButton = findViewById(R.id.saveBinaryButton);

        String[] opLabels = new String[]{
                "Addition (+)",
                "Subtraction (−)",
                "Multiplication (×)",
                "Division (÷)"
        };
        ArrayAdapter<String> opAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opLabels);
        opAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opSpinner.setAdapter(opAdapter);

        operandAInput.addTextChangedListener(new DecimalPreviewWatcher(operandAInput, operandADecimalPreview));
        operandBInput.addTextChangedListener(new DecimalPreviewWatcher(operandBInput, operandBDecimalPreview));

        findViewById(R.id.binaryBack).setOnClickListener(v -> finish());
        findViewById(R.id.calculateBinaryButton).setOnClickListener(v -> calculate());
        findViewById(R.id.clearBinaryButton).setOnClickListener(v -> clear());
        saveButton.setOnClickListener(v -> saveToHistory(true));

        findViewById(R.id.sampleAdd).setOnClickListener(v -> {
            opSpinner.setSelection(0);
            operandAInput.setText("1011");
            operandBInput.setText("0111");
            calculate();
        });

        findViewById(R.id.sampleSub).setOnClickListener(v -> {
            opSpinner.setSelection(1);
            operandAInput.setText("1100");
            operandBInput.setText("0101");
            calculate();
        });

        findViewById(R.id.sampleMul).setOnClickListener(v -> {
            opSpinner.setSelection(2);
            operandAInput.setText("1010");
            operandBInput.setText("11");
            calculate();
        });

        findViewById(R.id.sampleDiv).setOnClickListener(v -> {
            opSpinner.setSelection(3);
            operandAInput.setText("1101");
            operandBInput.setText("10");
            calculate();
        });

        handleReuseIntent();
    }

    private void handleReuseIntent() {
        if (getIntent() != null) {
            String op = getIntent().getStringExtra("REUSE_OP");
            String expr = getIntent().getStringExtra("REUSE_EXPRESSION");
            String opA = getIntent().getStringExtra("REUSE_A");
            String opB = getIntent().getStringExtra("REUSE_B");
            if (op != null) {
                for (int i = 0; i < BinaryArithmetic.Operation.values().length; i++) {
                    if (BinaryArithmetic.Operation.values()[i].getDisplayName().equalsIgnoreCase(op) ||
                            BinaryArithmetic.Operation.values()[i].name().equalsIgnoreCase(op)) {
                        opSpinner.setSelection(i);
                        break;
                    }
                }
            }
            if (opA != null && opB != null) {
                operandAInput.setText(opA);
                operandBInput.setText(opB);
                calculate();
            } else if (expr != null) {
                String[] parts = expr.split("[+\\-−×÷\\s]+");
                if (parts.length >= 2) {
                    operandAInput.setText(parts[0].trim());
                    operandBInput.setText(parts[1].trim());
                    calculate();
                }
            }
        }
    }

    private void calculate() {
        errorView.setVisibility(View.GONE);
        String inputA = operandAInput.getText().toString().trim();
        String inputB = operandBInput.getText().toString().trim();

        if (inputA.isEmpty() || inputB.isEmpty()) {
            showError("Please enter binary numbers for both operands.");
            return;
        }

        BinaryArithmetic.Operation op = BinaryArithmetic.Operation.values()[opSpinner.getSelectedItemPosition()];

        try {
            lastResult = BinaryArithmetic.calculate(op, inputA, inputB);
            displayResult(lastResult);
            saveToHistory(false);
        } catch (ArithmeticException e) {
            showError(e.getMessage());
            resetResults();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            resetResults();
        }
    }

    private void displayResult(BinaryArithmetic.Result res) {
        resultText.setText(res.getResultBinary());
        decimalResultText.setText(String.format("Decimal: %s", res.getResultDecimal()));

        if (res.getOperation() == BinaryArithmetic.Operation.DIVIDE && res.getRemainderBinary() != null) {
            remainderText.setVisibility(View.VISIBLE);
            remainderText.setText(String.format("Remainder: %s (Decimal: %s)",
                    res.getRemainderBinary(), res.getRemainderDecimal()));
        } else {
            remainderText.setVisibility(View.GONE);
        }

        alignedDisplay.setText(res.getAlignedDisplay());

        stepsContainer.removeAllViews();
        if (res.getSteps().isEmpty()) {
            stepsEmpty.setVisibility(View.VISIBLE);
            stepsContainer.addView(stepsEmpty);
        } else {
            stepsEmpty.setVisibility(View.GONE);
            for (String step : res.getSteps()) {
                TextView item = new TextView(this);
                item.setText("• " + step);
                item.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                item.setTextSize(13);
                item.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                        0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall));
                stepsContainer.addView(item);
            }
        }

        saveButton.setVisibility(View.VISIBLE);
    }

    private void saveToHistory(boolean showToast) {
        if (lastResult == null) return;
        StringBuilder sb = new StringBuilder("BIN ");
        sb.append(lastResult.getOperandABinary())
                .append(" ").append(lastResult.getOperation().getSymbol()).append(" ")
                .append(lastResult.getOperandBBinary())
                .append(" = ").append(lastResult.getResultBinary());
        if (lastResult.getRemainderBinary() != null && lastResult.getOperation() == BinaryArithmetic.Operation.DIVIDE) {
            sb.append(" R ").append(lastResult.getRemainderBinary());
        }
        sb.append(" [DEC: ").append(lastResult.getOperandADecimal())
                .append(" ").append(lastResult.getOperation().getSymbol()).append(" ")
                .append(lastResult.getOperandBDecimal())
                .append(" = ").append(lastResult.getResultDecimal());
        if (lastResult.getRemainderDecimal() != null && lastResult.getOperation() == BinaryArithmetic.Operation.DIVIDE) {
            sb.append(" R ").append(lastResult.getRemainderDecimal());
        }
        sb.append("]");

        CalculationHistory.record(this,
                com.example.cscalc.history.HistoryRepository.MODULE_BINARY,
                lastResult.getOperation().getDisplayName(),
                lastResult.getOperandABinary() + " " + lastResult.getOperation().getSymbol() + " " + lastResult.getOperandBBinary(),
                lastResult.getResultBinary() + " (DEC: " + lastResult.getResultDecimal() + ")"
                        + (lastResult.getRemainderBinary() != null ? " R " + lastResult.getRemainderBinary() : ""),
                "Binary Arithmetic",
                lastResult.getAlignedDisplay());
        if (showToast) {
            Toast.makeText(this, R.string.binary_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void clear() {
        operandAInput.setText("");
        operandBInput.setText("");
        operandADecimalPreview.setText("Decimal: —");
        operandBDecimalPreview.setText("Decimal: —");
        errorView.setVisibility(View.GONE);
        resetResults();
    }

    private void resetResults() {
        resultText.setText(R.string.result_placeholder);
        decimalResultText.setText("Decimal: —");
        remainderText.setVisibility(View.GONE);
        alignedDisplay.setText("Aligned carry/borrow steps will display here after calculation.");
        stepsContainer.removeAllViews();
        stepsEmpty.setVisibility(View.VISIBLE);
        stepsContainer.addView(stepsEmpty);
        saveButton.setVisibility(View.GONE);
        lastResult = null;
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private static class DecimalPreviewWatcher implements TextWatcher {
        private final EditText input;
        private final TextView preview;

        DecimalPreviewWatcher(EditText input, TextView preview) {
            this.input = input;
            this.preview = preview;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString().trim();
            if (text.isEmpty()) {
                preview.setText("Decimal: —");
                return;
            }
            try {
                BigInteger val = new BigInteger(text, 2);
                preview.setText("Decimal: " + val.toString(10));
            } catch (Exception e) {
                preview.setText("Decimal: (Invalid binary)");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
