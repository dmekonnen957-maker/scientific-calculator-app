package com.example.cscalc;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.cscalc.calculator.FlagRegisterCalculator;
import com.example.cscalc.history.HistoryRepository;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;

public class FlagRegisterActivity extends ToolkitActivity {
    private final String[] processorLabels = {"Intel 8085", "Intel 8086"};
    private final String[] operationLabels = {"ADD", "SUB", "CMP", "AND", "OR", "XOR"};
    private Spinner processorSpinner;
    private Spinner widthSpinner;
    private Spinner operationSpinner;
    private EditText operandAInput;
    private EditText operandBInput;
    private TextView errorView;
    private TextView binaryResult;
    private TextView decimalResult;
    private TextView hexResult;
    private TextView registerBits;
    private TextView explanation;
    private LinearLayout flagValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_register);
        SystemBarInsets.apply(findViewById(R.id.flagRoot));

        processorSpinner = findViewById(R.id.flagProcessorSpinner);
        widthSpinner = findViewById(R.id.flagWidthSpinner);
        operationSpinner = findViewById(R.id.flagOperationSpinner);
        operandAInput = findViewById(R.id.flagOperandA);
        operandBInput = findViewById(R.id.flagOperandB);
        errorView = findViewById(R.id.flagError);
        binaryResult = findViewById(R.id.flagResultBinary);
        decimalResult = findViewById(R.id.flagResultDecimal);
        hexResult = findViewById(R.id.flagResultHex);
        registerBits = findViewById(R.id.flagRegisterBits);
        flagValues = findViewById(R.id.flagValues);
        explanation = findViewById(R.id.flagExplanation);

        setAdapter(processorSpinner, processorLabels);
        setAdapter(operationSpinner, operationLabels);
        configureWidthSpinner(0);
        processorSpinner.setOnItemSelectedListener(new SimpleSelectionListener(
                position -> configureWidthSpinner(position)));

        findViewById(R.id.flagBack).setOnClickListener(view -> finish());
        findViewById(R.id.calculateFlagsButton).setOnClickListener(view -> calculate());
        findViewById(R.id.clearFlagsButton).setOnClickListener(view -> clear());
        findViewById(R.id.sampleFlagsButton).setOnClickListener(view -> loadSample());
        handleReuseIntent();
    }

    private void setAdapter(Spinner spinner, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void configureWidthSpinner(int processorPosition) {
        int oldSelection = widthSpinner.getSelectedItemPosition();
        String[] widths = processorPosition == 0
                ? new String[]{"8-bit"} : new String[]{"8-bit", "16-bit"};
        setAdapter(widthSpinner, widths);
        widthSpinner.setSelection(Math.min(oldSelection, widths.length - 1));
    }

    private void handleReuseIntent() {
        if (getIntent() == null || !getIntent().hasExtra("REUSE_A")
                || !getIntent().hasExtra("REUSE_B")) {
            return;
        }
        String processorName = getIntent().getStringExtra("REUSE_PROCESSOR");
        for (int index = 0; index < FlagRegisterCalculator.Processor.values().length; index++) {
            FlagRegisterCalculator.Processor processor =
                    FlagRegisterCalculator.Processor.values()[index];
            if (processor.getLabel().equalsIgnoreCase(processorName)
                    || processor.name().equalsIgnoreCase(processorName)) {
                processorSpinner.setSelection(index);
                configureWidthSpinner(index);
                break;
            }
        }

        if ("16".equals(getIntent().getStringExtra("REUSE_WIDTH"))) {
            widthSpinner.setSelection(1);
        } else {
            widthSpinner.setSelection(0);
        }

        String operationName = getIntent().getStringExtra("REUSE_OP");
        if (operationName != null) {
            for (int index = 0; index < FlagRegisterCalculator.Operation.values().length; index++) {
                FlagRegisterCalculator.Operation operation =
                        FlagRegisterCalculator.Operation.values()[index];
                if (operation.getLabel().equalsIgnoreCase(operationName)
                        || operation.name().equalsIgnoreCase(operationName)) {
                    operationSpinner.setSelection(index);
                    break;
                }
            }
        }
        operandAInput.setText(getIntent().getStringExtra("REUSE_A"));
        operandBInput.setText(getIntent().getStringExtra("REUSE_B"));
        calculate();
    }

    private void calculate() {
        String inputA = operandAInput.getText().toString().trim();
        String inputB = operandBInput.getText().toString().trim();
        if (TextUtils.isEmpty(inputA) || TextUtils.isEmpty(inputB)) {
            showError(getString(R.string.flags_error_empty));
            return;
        }
        if (inputA.length() > 6 || inputB.length() > 6) {
            showError(getString(R.string.flags_error_range));
            return;
        }

        try {
            FlagRegisterCalculator.Processor processor =
                    FlagRegisterCalculator.Processor.values()[processorSpinner.getSelectedItemPosition()];
            FlagRegisterCalculator.Operation operation =
                    FlagRegisterCalculator.Operation.valueOf(operationSpinner.getSelectedItem().toString());
            int width = widthSpinner.getSelectedItemPosition() == 0 ? 8 : 16;
            FlagRegisterCalculator.Result result = FlagRegisterCalculator.calculate(
                    processor, width, operation, new BigInteger(inputA), new BigInteger(inputB));
            showResult(result);
            saveToHistory(result);
            errorView.setVisibility(View.GONE);
        } catch (NumberFormatException exception) {
            showError(getString(R.string.flags_error_integer));
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void showResult(FlagRegisterCalculator.Result result) {
        int width = result.getWidth();
        String binary = leftPad(result.getValue().toString(2), width, '0');
        String hex = leftPad(result.getValue().toString(16).toUpperCase(Locale.ROOT),
                width / 4, '0');
        binaryResult.setText(binary);
        decimalResult.setText(getString(R.string.flags_decimal_result,
                result.getValue().toString(), result.getSignedValue().toString()));
        hexResult.setText(getString(R.string.flags_hex_result, hex));
        registerBits.setText(buildRegisterLayout(result));
        renderFlagValues(result.getFlags());
        explanation.setText(buildExplanation(result));
    }

    private String buildRegisterLayout(FlagRegisterCalculator.Result result) {
        StringBuilder bits = new StringBuilder();
        StringBuilder names = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (int bit = result.getWidth() - 1; bit >= 0; bit--) {
            String flag = flagNameAtBit(result.getProcessor(), bit);
            bits.append(String.format(Locale.ROOT, "%3d", bit));
            names.append(String.format(Locale.ROOT, "%3s", flag == null ? "-" : flag));
            Boolean state = flag == null ? null : result.getFlags().get(flag);
            values.append(String.format(Locale.ROOT, "%3s",
                    state == null ? (flag == null ? "-" : "?") : state ? "1" : "0"));
        }
        return getString(R.string.flags_register_layout, bits, names, values);
    }

    private String flagNameAtBit(FlagRegisterCalculator.Processor processor, int bit) {
        if (processor == FlagRegisterCalculator.Processor.INTEL_8085) {
            switch (bit) {
                case 7: return "S";
                case 6: return "Z";
                case 4: return "AC";
                case 2: return "P";
                case 0: return "CY";
                default: return null;
            }
        }
        switch (bit) {
            case 11: return "OF";
            case 7: return "SF";
            case 6: return "ZF";
            case 4: return "AF";
            case 2: return "PF";
            case 0: return "CF";
            default: return null;
        }
    }

    private void renderFlagValues(Map<String, Boolean> flags) {
        flagValues.removeAllViews();
        LinearLayout row = null;
        int index = 0;
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            if (index % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                flagValues.addView(row);
            }
            TextView flag = new TextView(this);
            String state = entry.getValue() == null ? getString(R.string.flags_undefined)
                    : entry.getValue() ? getString(R.string.flags_set) : getString(R.string.flags_clear);
            flag.setText(getString(R.string.flags_register_item, entry.getKey(), state));
            flag.setGravity(Gravity.CENTER);
            flag.setTextSize(12);
            flag.setPadding(8, 10, 8, 10);
            flag.setTextColor(ContextCompat.getColor(this,
                    Boolean.TRUE.equals(entry.getValue()) ? R.color.primary_blue : R.color.text_secondary));
            flag.setBackgroundResource(Boolean.TRUE.equals(entry.getValue())
                    ? R.drawable.bg_welcome : R.drawable.bg_tool_card);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                    getResources().getDimensionPixelSize(R.dimen.spacing_xsmall));
            row.addView(flag, params);
            index++;
        }
    }

    private String buildExplanation(FlagRegisterCalculator.Result result) {
        String operation = result.getOperation().getLabel();
        String arithmeticRule;
        if (result.getOperation() == FlagRegisterCalculator.Operation.ADD) {
            arithmeticRule = getString(R.string.flags_add_explanation);
        } else if (result.getOperation() == FlagRegisterCalculator.Operation.SUB
                || result.getOperation() == FlagRegisterCalculator.Operation.CMP) {
            arithmeticRule = getString(R.string.flags_sub_explanation);
        } else {
            arithmeticRule = getString(R.string.flags_logic_explanation);
        }
        String compareNote = result.isCompareOnly()
                ? "\n" + getString(R.string.flags_compare_note) : "";
        String afNote = result.getFlags().containsKey("AF")
                && result.getFlags().get("AF") == null
                ? "\n" + getString(R.string.flags_af_undefined_note) : "";
        String carryName = result.getProcessor() == FlagRegisterCalculator.Processor.INTEL_8085
                ? "CY" : "CF";
        String overflowNote = result.getProcessor() == FlagRegisterCalculator.Processor.INTEL_8086
                ? "\n" + getString(R.string.flags_overflow_explanation, carryName)
                : "\n" + getString(R.string.flags_8085_overflow_explanation);
        return getString(R.string.flags_calculation_explanation,
                result.getOperandA(), operation, result.getOperandB(), result.getWidth(),
                result.getValue(), arithmeticRule, carryName) + compareNote + afNote + overflowNote
                + "\n" + getString(R.string.flags_control_note);
    }

    private void saveToHistory(FlagRegisterCalculator.Result result) {
        StringBuilder flagText = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : result.getFlags().entrySet()) {
            if (flagText.length() > 0) flagText.append(", ");
            flagText.append(entry.getKey()).append('=')
                    .append(entry.getValue() == null ? "undefined" : entry.getValue() ? "1" : "0");
        }
        CalculationHistory.record(this, HistoryRepository.MODULE_FLAGS,
                result.getOperation().getLabel(),
                result.getOperandA() + " " + result.getOperation().getLabel() + " " + result.getOperandB(),
                result.getValue().toString(), result.getProcessor().getLabel() + ", " + result.getWidth() + "-bit",
                flagText.toString());
    }

    private void loadSample() {
        processorSpinner.setSelection(1);
        configureWidthSpinner(1);
        widthSpinner.setSelection(0);
        operationSpinner.setSelection(0);
        operandAInput.setText("127");
        operandBInput.setText("1");
        calculate();
    }

    private void clear() {
        operandAInput.setText("");
        operandBInput.setText("");
        errorView.setVisibility(View.GONE);
        binaryResult.setText(R.string.result_placeholder);
        decimalResult.setText("");
        hexResult.setText("");
        registerBits.setText(R.string.flags_register_prompt);
        flagValues.removeAllViews();
        explanation.setText(R.string.flags_explanation_prompt);
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private String leftPad(String value, int width, char fill) {
        StringBuilder padded = new StringBuilder(value);
        while (padded.length() < width) padded.insert(0, fill);
        return padded.toString();
    }

    private interface SelectionAction {
        void onSelected(int position);
    }

    private static final class SimpleSelectionListener
            implements android.widget.AdapterView.OnItemSelectedListener {
        private final SelectionAction action;

        private SimpleSelectionListener(SelectionAction action) {
            this.action = action;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                   int position, long id) {
            action.onSelected(position);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
