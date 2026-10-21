package com.example.cscalc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cscalc.calculator.BitwiseOps;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

public class BitwiseOperationsActivity extends ToolkitActivity {

    private Spinner widthSpinner;
    private Spinner opSpinner;
    private EditText operandAInput;
    private EditText operandBInput;
    private TextView operandBLabel;
    private TextView operandAPreview;
    private TextView operandBPreview;
    private TextView errorView;

    private TextView binaryResultText;
    private TextView hexResultText;
    private TextView decimalResultText;
    private View saveButton;

    private TextView bitGridText;
    private TextView maskingExplanationText;
    private TextView signednessExplanationText;
    private TextView shiftExplanationText;

    private BitwiseOps.Result lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitwise_operations);
        SystemBarInsets.apply(findViewById(R.id.bitwiseRoot));

        widthSpinner = findViewById(R.id.bitWidthSpinner);
        opSpinner = findViewById(R.id.bitwiseOpSpinner);
        operandAInput = findViewById(R.id.bitwiseOperandA);
        operandBInput = findViewById(R.id.bitwiseOperandB);
        operandBLabel = findViewById(R.id.operandBLabel);
        operandAPreview = findViewById(R.id.operandAPreview);
        operandBPreview = findViewById(R.id.operandBPreview);
        errorView = findViewById(R.id.bitwiseError);

        binaryResultText = findViewById(R.id.bitwiseBinaryResult);
        hexResultText = findViewById(R.id.bitwiseHexResult);
        decimalResultText = findViewById(R.id.bitwiseDecimalResult);
        saveButton = findViewById(R.id.saveBitwiseButton);

        bitGridText = findViewById(R.id.bitwiseBitGrid);
        maskingExplanationText = findViewById(R.id.bitwiseMaskingExplanation);
        signednessExplanationText = findViewById(R.id.bitwiseSignednessExplanation);
        shiftExplanationText = findViewById(R.id.bitwiseShiftExplanation);

        String[] widthLabels = new String[]{
                "8-bit (Byte, max 0xFF)",
                "16-bit (Word, max 0xFFFF)",
                "32-bit (Dword, max 0xFFFFFFFF)"
        };
        ArrayAdapter<String> widthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, widthLabels);
        widthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        widthSpinner.setAdapter(widthAdapter);

        String[] opLabels = new String[]{
                "AND (&)",
                "OR (|)",
                "XOR (^)",
                "NOT (~) [Bounded]",
                "Left Shift (<<)",
                "Logical Right Shift (>>>)",
                "Arithmetic Right Shift (>>)"
        };
        ArrayAdapter<String> opAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opLabels);
        opAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opSpinner.setAdapter(opAdapter);

        opSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BitwiseOps.Operation op = BitwiseOps.Operation.values()[position];
                if (op.isUnary()) {
                    operandBInput.setVisibility(View.GONE);
                    operandBLabel.setVisibility(View.GONE);
                    operandBPreview.setVisibility(View.GONE);
                } else {
                    operandBInput.setVisibility(View.VISIBLE);
                    operandBLabel.setVisibility(View.VISIBLE);
                    operandBPreview.setVisibility(View.VISIBLE);
                    if (op == BitwiseOps.Operation.LEFT_SHIFT ||
                            op == BitwiseOps.Operation.RIGHT_SHIFT_LOGICAL ||
                            op == BitwiseOps.Operation.RIGHT_SHIFT_ARITHMETIC) {
                        operandBLabel.setText("Shift Amount (bits)");
                        operandBInput.setHint("e.g. 1 or 2");
                    } else {
                        operandBLabel.setText(R.string.bitwise_operand_b);
                        operandBInput.setHint(R.string.bitwise_input_hint);
                    }
                }
                updatePreviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        widthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePreviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        operandAInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreviews();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        operandBInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreviews();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.bitwiseBack).setOnClickListener(v -> finish());
        findViewById(R.id.calculateBitwiseButton).setOnClickListener(v -> calculate());
        findViewById(R.id.clearBitwiseButton).setOnClickListener(v -> clear());
        saveButton.setOnClickListener(v -> saveToHistory(true));

        findViewById(R.id.sampleAnd).setOnClickListener(v -> {
            widthSpinner.setSelection(0);
            opSpinner.setSelection(0);
            operandAInput.setText("0x0F");
            operandBInput.setText("0x55");
            calculate();
        });

        findViewById(R.id.sampleXor).setOnClickListener(v -> {
            widthSpinner.setSelection(0);
            opSpinner.setSelection(2);
            operandAInput.setText("0xAA");
            operandBInput.setText("0x55");
            calculate();
        });

        findViewById(R.id.sampleNot).setOnClickListener(v -> {
            widthSpinner.setSelection(0);
            opSpinner.setSelection(3);
            operandAInput.setText("0x01");
            calculate();
        });

        findViewById(R.id.sampleShl).setOnClickListener(v -> {
            widthSpinner.setSelection(0);
            opSpinner.setSelection(4);
            operandAInput.setText("0x07");
            operandBInput.setText("2");
            calculate();
        });

        findViewById(R.id.sampleSar).setOnClickListener(v -> {
            widthSpinner.setSelection(0);
            opSpinner.setSelection(6);
            operandAInput.setText("0x80");
            operandBInput.setText("1");
            calculate();
        });

        handleReuseIntent();
    }

    private void handleReuseIntent() {
        if (getIntent() != null) {
            String widthStr = getIntent().getStringExtra("REUSE_WIDTH");
            String op = getIntent().getStringExtra("REUSE_OP");
            String inputA = getIntent().getStringExtra("REUSE_INPUT");
            String opA = getIntent().getStringExtra("REUSE_A");
            String opB = getIntent().getStringExtra("REUSE_B");
            if (widthStr != null) {
                for (int i = 0; i < BitwiseOps.BitWidth.values().length; i++) {
                    if (widthStr.contains(String.valueOf(BitwiseOps.BitWidth.values()[i].getBits()))) {
                        widthSpinner.setSelection(i);
                        break;
                    }
                }
            }
            if (op != null) {
                for (int i = 0; i < BitwiseOps.Operation.values().length; i++) {
                    if (BitwiseOps.Operation.values()[i].getDisplayName().equalsIgnoreCase(op) ||
                            BitwiseOps.Operation.values()[i].getSymbol().equalsIgnoreCase(op)) {
                        opSpinner.setSelection(i);
                        break;
                    }
                }
            }
            if (opA != null) {
                operandAInput.setText(opA);
                if (opB != null) operandBInput.setText(opB);
                calculate();
            } else if (inputA != null) {
                String[] parts = inputA.split("[&|^~<<>>>>>\\s]+");
                if (parts.length >= 1 && !parts[0].trim().isEmpty()) {
                    operandAInput.setText(parts[0].trim());
                    if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                        operandBInput.setText(parts[1].trim());
                    }
                    calculate();
                }
            }
        }
    }

    private BitwiseOps.BitWidth getSelectedWidth() {
        return BitwiseOps.BitWidth.values()[widthSpinner.getSelectedItemPosition()];
    }

    private void updatePreviews() {
        BitwiseOps.BitWidth width = getSelectedWidth();
        String textA = operandAInput.getText().toString().trim();
        if (textA.isEmpty()) {
            operandAPreview.setText("BIN: —  |  HEX: —");
        } else {
            try {
                long valA = BitwiseOps.parseInput(textA, width, 10);
                operandAPreview.setText("BIN: " + BitwiseOps.formatBinary(valA, width) + "  |  HEX: " + BitwiseOps.formatHex(valA, width));
            } catch (Exception e) {
                operandAPreview.setText("BIN: (Invalid)  |  HEX: (Invalid)");
            }
        }

        BitwiseOps.Operation op = BitwiseOps.Operation.values()[opSpinner.getSelectedItemPosition()];
        if (!op.isUnary()) {
            String textB = operandBInput.getText().toString().trim();
            if (textB.isEmpty()) {
                operandBPreview.setText("BIN: —  |  HEX: —");
            } else {
                try {
                    long valB = BitwiseOps.parseInput(textB, width, 10);
                    operandBPreview.setText("BIN: " + BitwiseOps.formatBinary(valB, width) + "  |  HEX: " + BitwiseOps.formatHex(valB, width));
                } catch (Exception e) {
                    operandBPreview.setText("BIN: (Invalid)  |  HEX: (Invalid)");
                }
            }
        }
    }

    private void calculate() {
        errorView.setVisibility(View.GONE);
        BitwiseOps.BitWidth width = getSelectedWidth();
        BitwiseOps.Operation op = BitwiseOps.Operation.values()[opSpinner.getSelectedItemPosition()];

        String textA = operandAInput.getText().toString().trim();
        if (textA.isEmpty()) {
            showError("Operand A cannot be empty.");
            return;
        }

        long valA;
        try {
            valA = BitwiseOps.parseInput(textA, width, 10);
        } catch (IllegalArgumentException e) {
            showError("Invalid format for Operand A: " + e.getMessage());
            return;
        }

        Long valB = null;
        if (!op.isUnary()) {
            String textB = operandBInput.getText().toString().trim();
            if (textB.isEmpty()) {
                showError("Operand B cannot be empty.");
                return;
            }
            try {
                valB = BitwiseOps.parseInput(textB, width, 10);
            } catch (IllegalArgumentException e) {
                showError("Invalid format for Operand B: " + e.getMessage());
                return;
            }
        }

        try {
            lastResult = BitwiseOps.calculate(op, width, valA, valB);
            displayResult(lastResult);
            saveToHistory(false);
        } catch (Exception e) {
            showError("Calculation error: " + e.getMessage());
        }
    }

    private void displayResult(BitwiseOps.Result res) {
        binaryResultText.setText(res.getBinaryResult());
        hexResultText.setText(String.format("HEX: %s", res.getHexResult()));
        decimalResultText.setText(String.format("Unsigned: %d  |  Signed: %d",
                res.getUnsignedResult(), res.getSignedResult()));

        bitGridText.setText(res.getBitGridComparison());
        maskingExplanationText.setText(res.getMaskingExplanation());
        signednessExplanationText.setText(res.getSignednessExplanation());

        StringBuilder shiftSb = new StringBuilder();
        for (String step : res.getSteps()) {
            if (shiftSb.length() > 0) shiftSb.append("\n• ");
            else shiftSb.append("• ");
            shiftSb.append(step);
        }
        shiftExplanationText.setText(shiftSb.toString());

        saveButton.setVisibility(View.VISIBLE);
    }

    private void saveToHistory(boolean showToast) {
        if (lastResult == null) return;
        StringBuilder sb = new StringBuilder("BITWISE [");
        sb.append(lastResult.getBitWidth().getBits()).append("-bit] ");
        sb.append(lastResult.getHexA()).append(" ");
        sb.append(lastResult.getOperation().getSymbol()).append(" ");
        if (lastResult.getHexB() != null) {
            sb.append(lastResult.getHexB()).append(" ");
        }
        sb.append("= ").append(lastResult.getHexResult());
        sb.append(" (BIN: ").append(lastResult.getBinaryResult()).append(")");

        CalculationHistory.record(this,
                com.example.cscalc.history.HistoryRepository.MODULE_BITWISE,
                lastResult.getOperation().getDisplayName(),
                (lastResult.getOperation().isUnary() ? "~" + lastResult.getHexA() : lastResult.getHexA() + " " + lastResult.getOperation().getSymbol() + " " + (lastResult.getHexB() != null ? lastResult.getHexB() : "")),
                lastResult.getHexResult() + " (BIN: " + lastResult.getBinaryResult() + ")",
                lastResult.getBitWidth().getLabel(),
                lastResult.getBitGridComparison() + "\n" + lastResult.getMaskingExplanation());
        if (showToast) {
            Toast.makeText(this, R.string.bitwise_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void clear() {
        operandAInput.setText("");
        operandBInput.setText("");
        errorView.setVisibility(View.GONE);
        binaryResultText.setText(R.string.result_placeholder);
        hexResultText.setText("HEX: —");
        decimalResultText.setText("Unsigned: —  |  Signed: —");
        bitGridText.setText("Bit alignment comparison will appear here.");
        maskingExplanationText.setText("Masking explanation will appear here.");
        signednessExplanationText.setText("Signedness interpretation will appear here.");
        shiftExplanationText.setText("Shift and operator explanations will appear here.");
        saveButton.setVisibility(View.GONE);
        lastResult = null;
        updatePreviews();
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }
}
