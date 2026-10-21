package com.example.cscalc;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cscalc.calculator.MemoryUnitConverter;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MemoryUnitsActivity extends ToolkitActivity {

    private Spinner workedExampleSpinner;
    private EditText quantityInput;
    private Spinner sourceUnitSpinner;
    private Spinner targetUnitSpinner;
    private TextView errorView;
    private TextView resultText;
    private TextView exactBytesText;
    private TextView distinctionText;
    private LinearLayout stepsContainer;
    private TextView stepsEmpty;
    private LinearLayout equivalentsContainer;
    private TextView equivalentsEmpty;
    private View saveButton;

    private MemoryUnitConverter.ConversionResult lastResult;
    private boolean isUserSelectingExample = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_units);
        SystemBarInsets.apply(findViewById(R.id.memoryRoot));

        workedExampleSpinner = findViewById(R.id.workedExampleSpinner);
        quantityInput = findViewById(R.id.memoryQuantityInput);
        sourceUnitSpinner = findViewById(R.id.sourceUnitSpinner);
        targetUnitSpinner = findViewById(R.id.targetUnitSpinner);
        errorView = findViewById(R.id.memoryError);
        resultText = findViewById(R.id.memoryResultText);
        exactBytesText = findViewById(R.id.memoryExactBytesText);
        distinctionText = findViewById(R.id.memoryDistinctionText);
        stepsContainer = findViewById(R.id.memoryStepsContainer);
        stepsEmpty = findViewById(R.id.memoryStepsEmpty);
        equivalentsContainer = findViewById(R.id.memoryEquivalentsContainer);
        equivalentsEmpty = findViewById(R.id.memoryEquivalentsEmpty);
        saveButton = findViewById(R.id.saveMemoryButton);

        // Unit labels for Spinners
        MemoryUnitConverter.Unit[] units = MemoryUnitConverter.Unit.values();
        String[] unitLabels = new String[units.length];
        for (int i = 0; i < units.length; i++) {
            unitLabels[i] = units[i].getSymbol() + " — " + units[i].getName();
        }

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, unitLabels);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceUnitSpinner.setAdapter(unitAdapter);
        targetUnitSpinner.setAdapter(unitAdapter);

        // Default: from TB (index 8) to GiB (index 14)
        sourceUnitSpinner.setSelection(findUnitIndex(MemoryUnitConverter.Unit.TERABYTE));
        targetUnitSpinner.setSelection(findUnitIndex(MemoryUnitConverter.Unit.GIBIBYTE));

        // Worked examples spinner
        List<MemoryUnitConverter.WorkedExample> examples = MemoryUnitConverter.getWorkedExamples();
        String[] exampleTitles = new String[examples.size() + 1];
        exampleTitles[0] = "Select a worked example to load...";
        for (int i = 0; i < examples.size(); i++) {
            exampleTitles[i + 1] = examples.get(i).getTitle();
        }

        ArrayAdapter<String> exampleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, exampleTitles);
        exampleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workedExampleSpinner.setAdapter(exampleAdapter);

        workedExampleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                MemoryUnitConverter.WorkedExample ex = examples.get(position - 1);
                quantityInput.setText(ex.getValue().toPlainString());
                sourceUnitSpinner.setSelection(findUnitIndex(ex.getSourceUnit()));
                targetUnitSpinner.setSelection(findUnitIndex(ex.getTargetUnit()));
                convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.memoryBack).setOnClickListener(v -> finish());
        findViewById(R.id.convertMemoryButton).setOnClickListener(v -> convert());
        findViewById(R.id.clearMemoryButton).setOnClickListener(v -> clear());
        saveButton.setOnClickListener(v -> saveToHistory(true));

        findViewById(R.id.swapUnitsButton).setOnClickListener(v -> {
            int src = sourceUnitSpinner.getSelectedItemPosition();
            int tgt = targetUnitSpinner.getSelectedItemPosition();
            sourceUnitSpinner.setSelection(tgt);
            targetUnitSpinner.setSelection(src);
            if (!quantityInput.getText().toString().trim().isEmpty()) {
                convert();
            }
        });

        // Initialize with default sample
        quantityInput.setText("1");
        convert();
        handleReuseIntent();
    }

    private void handleReuseIntent() {
        if (getIntent() != null) {
            String input = getIntent().getStringExtra("REUSE_INPUT");
            String config = getIntent().getStringExtra("REUSE_CONFIG");
            if (config != null && config.contains("→")) {
                String[] parts = config.split("→");
                if (parts.length >= 2) {
                    String src = parts[0].trim();
                    String tgt = parts[1].split("\\s+")[0].trim();
                    for (int i = 0; i < MemoryUnitConverter.Unit.values().length; i++) {
                        if (MemoryUnitConverter.Unit.values()[i].getSymbol().equalsIgnoreCase(src)) {
                            sourceUnitSpinner.setSelection(i);
                        }
                        if (MemoryUnitConverter.Unit.values()[i].getSymbol().equalsIgnoreCase(tgt)) {
                            targetUnitSpinner.setSelection(i);
                        }
                    }
                }
            }
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split("\\s+");
                quantityInput.setText(parts[0].replace(",", ""));
                convert();
            }
        }
    }

    private int findUnitIndex(MemoryUnitConverter.Unit unit) {
        MemoryUnitConverter.Unit[] all = MemoryUnitConverter.Unit.values();
        for (int i = 0; i < all.length; i++) {
            if (all[i] == unit) return i;
        }
        return 0;
    }

    private void convert() {
        errorView.setVisibility(View.GONE);
        String text = quantityInput.getText().toString().trim();
        if (text.isEmpty()) {
            showError("Please enter a numeric quantity.");
            return;
        }

        BigDecimal qty;
        try {
            qty = new BigDecimal(text);
            if (qty.compareTo(BigDecimal.ZERO) < 0) {
                showError("Quantity must be a non-negative number.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid number format: " + text);
            return;
        }

        MemoryUnitConverter.Unit source = MemoryUnitConverter.Unit.values()[sourceUnitSpinner.getSelectedItemPosition()];
        MemoryUnitConverter.Unit target = MemoryUnitConverter.Unit.values()[targetUnitSpinner.getSelectedItemPosition()];

        try {
            lastResult = MemoryUnitConverter.convert(qty, source, target);
            displayResult(lastResult);
            saveToHistory(false);
        } catch (Exception e) {
            showError("Conversion error: " + e.getMessage());
        }
    }

    private void displayResult(MemoryUnitConverter.ConversionResult res) {
        resultText.setText(res.getFormattedTarget());
        exactBytesText.setText(String.format("Exact Base Units: %s Bytes  |  %s Bits",
                MemoryUnitConverter.formatNumber(res.getExactBytes()),
                MemoryUnitConverter.formatNumber(res.getExactBits())));

        distinctionText.setText(res.getDistinctionExplanation());

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

        equivalentsContainer.removeAllViews();
        if (res.getEquivalentValues().isEmpty()) {
            equivalentsEmpty.setVisibility(View.VISIBLE);
            equivalentsContainer.addView(equivalentsEmpty);
        } else {
            equivalentsEmpty.setVisibility(View.GONE);
            for (Map.Entry<MemoryUnitConverter.Unit, String> entry : res.getEquivalentValues().entrySet()) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                        0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall));

                TextView label = new TextView(this);
                label.setText(entry.getKey().getName() + ":");
                label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                label.setTextSize(13);
                label.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                TextView value = new TextView(this);
                value.setText(entry.getValue());
                value.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                value.setTextSize(13);
                value.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                value.setTypeface(null, android.graphics.Typeface.BOLD);

                row.addView(label);
                row.addView(value);
                equivalentsContainer.addView(row);
            }
        }

        saveButton.setVisibility(View.VISIBLE);
    }

    private void saveToHistory(boolean showToast) {
        if (lastResult == null) return;
        String entry = "MEMORY: " + MemoryUnitConverter.formatNumber(lastResult.getInputValue())
                + " " + lastResult.getSourceUnit().getSymbol()
                + " = " + lastResult.getFormattedTarget()
                + " (" + MemoryUnitConverter.formatNumber(lastResult.getExactBytes()) + " B)";
        CalculationHistory.record(this,
                com.example.cscalc.history.HistoryRepository.MODULE_MEMORY,
                "Unit Conversion",
                MemoryUnitConverter.formatNumber(lastResult.getInputValue()) + " " + lastResult.getSourceUnit().getSymbol(),
                lastResult.getFormattedTarget(),
                lastResult.getSourceUnit().getSymbol() + " → " + lastResult.getTargetUnit().getSymbol(),
                "Exact Base Units: " + MemoryUnitConverter.formatNumber(lastResult.getExactBytes()) + " Bytes | "
                        + MemoryUnitConverter.formatNumber(lastResult.getExactBits()) + " Bits\n"
                        + lastResult.getDistinctionExplanation());
        if (showToast) {
            Toast.makeText(this, R.string.memory_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void clear() {
        quantityInput.setText("");
        errorView.setVisibility(View.GONE);
        resultText.setText(R.string.result_placeholder);
        exactBytesText.setText("Exact bytes: —");
        distinctionText.setText("Distinction between decimal (1000) and binary (1024) units will appear here.");
        stepsContainer.removeAllViews();
        stepsEmpty.setVisibility(View.VISIBLE);
        stepsContainer.addView(stepsEmpty);
        equivalentsContainer.removeAllViews();
        equivalentsEmpty.setVisibility(View.VISIBLE);
        equivalentsContainer.addView(equivalentsEmpty);
        saveButton.setVisibility(View.GONE);
        workedExampleSpinner.setSelection(0);
        lastResult = null;
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }
}
