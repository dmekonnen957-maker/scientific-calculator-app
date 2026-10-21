package com.example.cscalc;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cscalc.converter.NumberBase;
import com.example.cscalc.converter.BaseConverter;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.InputValidator;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

public class NumberConverterActivity extends ToolkitActivity {
    private EditText input;
    private Spinner baseSpinner;
    private TextView binaryResult;
    private TextView octalResult;
    private TextView decimalResult;
    private TextView hexadecimalResult;
    private TextView explanation;
    private TextView[] resultValues;
    private int[] resultRowIds;
    private NumberBase[] resultBases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_converter);
        SystemBarInsets.apply(findViewById(R.id.converterRoot));

        input = findViewById(R.id.numberInput);
        baseSpinner = findViewById(R.id.baseSpinner);
        binaryResult = findResultValue(R.id.resultRowBinary);
        octalResult = findResultValue(R.id.resultRowOctal);
        decimalResult = findResultValue(R.id.resultRowDecimal);
        hexadecimalResult = findResultValue(R.id.resultRowHexadecimal);
        explanation = findViewById(R.id.conversionExplanation);
        resultValues = new TextView[]{binaryResult, octalResult, decimalResult, hexadecimalResult};
        resultRowIds = new int[]{R.id.resultRowBinary, R.id.resultRowOctal,
                R.id.resultRowDecimal, R.id.resultRowHexadecimal};
        resultBases = new NumberBase[]{NumberBase.BINARY, NumberBase.OCTAL,
                NumberBase.DECIMAL, NumberBase.HEXADECIMAL};
        setResultLabel(R.id.resultRowBinary, R.string.label_binary);
        setResultLabel(R.id.resultRowOctal, R.string.label_octal);
        setResultLabel(R.id.resultRowDecimal, R.string.label_decimal);
        setResultLabel(R.id.resultRowHexadecimal, R.string.label_hex);

        String[] bases = new String[NumberBase.values().length];
        for (int index = 0; index < NumberBase.values().length; index++) {
            bases[index] = NumberBase.values()[index].getShortName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bases);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        baseSpinner.setAdapter(adapter);

        findViewById(R.id.converterBack).setOnClickListener(view -> finish());
        findViewById(R.id.convertButton).setOnClickListener(view -> convert(true));
        findViewById(R.id.clearButton).setOnClickListener(view -> clear());
        findViewById(R.id.sampleBinary).setOnClickListener(
                view -> useSample("101101", NumberBase.BINARY));
        findViewById(R.id.sampleOctal).setOnClickListener(
                view -> useSample("755", NumberBase.OCTAL));
        findViewById(R.id.sampleDecimal).setOnClickListener(
                view -> useSample("255", NumberBase.DECIMAL));
        findViewById(R.id.sampleHexadecimal).setOnClickListener(
                view -> useSample("2F", NumberBase.HEXADECIMAL));

        for (int index = 0; index < resultRowIds.length; index++) {
            final int rowIndex = index;
            View row = findViewById(resultRowIds[index]);
            TextView copyButton = row.findViewById(R.id.copyResultButton);
            copyButton.setContentDescription(getString(R.string.copy_result_accessibility,
                    getBaseName(resultBases[index])));
            copyButton.setOnClickListener(view -> copyResult(rowIndex));
            TextView useButton = row.findViewById(R.id.useResultButton);
            useButton.setContentDescription(getString(R.string.use_result_accessibility,
                    getBaseName(resultBases[index])));
            useButton.setOnClickListener(view -> useResultAsSource(rowIndex));
            setResultActionsEnabled(rowIndex, false);
        }

        handleReuseIntent();
    }

    private void handleReuseIntent() {
        if (getIntent() != null && getIntent().hasExtra("REUSE_INPUT")) {
            String reuseInput = getIntent().getStringExtra("REUSE_INPUT");
            String reuseBase = getIntent().getStringExtra("REUSE_BASE");
            if (reuseInput != null && !reuseInput.isEmpty()) {
                input.setText(reuseInput);
                if (reuseBase != null) {
                    for (int i = 0; i < NumberBase.values().length; i++) {
                        if (NumberBase.values()[i].getShortName().equalsIgnoreCase(reuseBase) ||
                                NumberBase.values()[i].name().equalsIgnoreCase(reuseBase)) {
                            baseSpinner.setSelection(i);
                            break;
                        }
                    }
                }
                convert(false);
            }
        }
    }

    private void convert(boolean saveHistory) {
        String text = input.getText().toString().trim();
        NumberBase source = NumberBase.values()[baseSpinner.getSelectedItemPosition()];
        InputValidator.ValidationError validation = InputValidator.validate(text, source);
        if (validation != InputValidator.ValidationError.NONE) {
            resetResults();
            input.setError(getValidationMessage(validation));
            return;
        }
        try {
            String[] results = new String[resultBases.length];
            for (int index = 0; index < resultBases.length; index++) {
                results[index] = BaseConverter.convert(text, source, resultBases[index]);
                resultValues[index].setText(results[index]);
                setResultActionsEnabled(index, true);
            }
            explanation.setText(BaseConverter.explain(text, source));
            if (saveHistory) {
                String fullResult = "BIN " + results[0] + "  |  OCT " + results[1]
                        + "  |  DEC " + results[2] + "  |  HEX " + results[3];
                CalculationHistory.record(this,
                        com.example.cscalc.history.HistoryRepository.MODULE_CONVERTER,
                        "Base Conversion",
                        text,
                        fullResult,
                        "Source: " + source.getShortName(),
                        BaseConverter.explain(text, source));
            }
            input.setError(null);
            View focused = getCurrentFocus();
            if (focused != null) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        } catch (NumberFormatException exception) {
            resetResults();
            input.setError(getString(R.string.error_invalid_input));
        }
    }

    private void clear() {
        input.setText("");
        input.setError(null);
        resetResults();
        Toast.makeText(this, R.string.converter_cleared, Toast.LENGTH_SHORT).show();
    }

    private void resetResults() {
        binaryResult.setText(R.string.result_placeholder);
        octalResult.setText(R.string.result_placeholder);
        decimalResult.setText(R.string.result_placeholder);
        hexadecimalResult.setText(R.string.result_placeholder);
        explanation.setText(R.string.explanation_prompt);
        for (int index = 0; index < resultRowIds.length; index++) {
            setResultActionsEnabled(index, false);
        }
    }

    private void setResultLabel(int rowId, int labelId) {
        View row = findViewById(rowId);
        ((TextView) row.findViewById(R.id.resultLabel)).setText(labelId);
    }

    private TextView findResultValue(int rowId) {
        View row = findViewById(rowId);
        return row.findViewById(R.id.resultValue);
    }

    private void copyResult(int index) {
        String value = resultValues[index].getText().toString();
        if (value.isEmpty() || getString(R.string.result_placeholder).equals(value)) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(
                getBaseName(resultBases[index]), value));
        Toast.makeText(this, R.string.result_copied, Toast.LENGTH_SHORT).show();
    }

    private void useResultAsSource(int index) {
        input.setText(resultValues[index].getText());
        baseSpinner.setSelection(index);
        convert(false);
    }

    private void useSample(String value, NumberBase base) {
        input.setText(value);
        baseSpinner.setSelection(base.ordinal());
        convert(true);
    }

    private void setResultActionsEnabled(int index, boolean enabled) {
        View row = findViewById(resultRowIds[index]);
        View copyButton = row.findViewById(R.id.copyResultButton);
        View useButton = row.findViewById(R.id.useResultButton);
        copyButton.setEnabled(enabled);
        copyButton.setAlpha(enabled ? 1f : 0.4f);
        useButton.setEnabled(enabled);
        useButton.setAlpha(enabled ? 1f : 0.4f);
    }

    private String getValidationMessage(InputValidator.ValidationError error) {
        switch (error) {
            case EMPTY:
                return getString(R.string.error_empty_input);
            case TOO_LONG:
                return getString(R.string.error_input_too_long,
                        InputValidator.MAX_INPUT_DIGITS);
            case INVALID_DIGIT:
                return getString(R.string.error_invalid_input);
            default:
                return null;
        }
    }

    private String getBaseName(NumberBase base) {
        switch (base) {
            case BINARY:
                return getString(R.string.label_binary);
            case OCTAL:
                return getString(R.string.label_octal);
            case DECIMAL:
                return getString(R.string.label_decimal);
            case HEXADECIMAL:
                return getString(R.string.label_hex);
            default:
                throw new IllegalArgumentException("Unsupported number base");
        }
    }
}
