package com.example.cscalc;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.example.cscalc.calculator.LogicGateCalculator;
import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.util.List;

public class LogicGatesActivity extends AppCompatActivity {
    private Spinner gateSpinner;
    private SwitchCompat inputA;
    private SwitchCompat inputB;
    private TextView inputBLabel;
    private TextView inputAValue;
    private TextView inputBValue;
    private TextView liveOutput;
    private TextView expressionText;
    private LinearLayout truthTable;
    private LogicGateCalculator.Gate selectedGate = LogicGateCalculator.Gate.AND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logic_gates);
        SystemBarInsets.apply(findViewById(R.id.logicRoot));

        gateSpinner = findViewById(R.id.gateSpinner);
        inputA = findViewById(R.id.gateInputA);
        inputB = findViewById(R.id.gateInputB);
        inputBLabel = findViewById(R.id.gateInputBLabel);
        inputAValue = findViewById(R.id.gateInputAValue);
        inputBValue = findViewById(R.id.gateInputBValue);
        liveOutput = findViewById(R.id.gateLiveOutput);
        expressionText = findViewById(R.id.gateExpression);
        truthTable = findViewById(R.id.gateTruthTable);

        String[] labels = new String[LogicGateCalculator.Gate.values().length];
        for (int index = 0; index < labels.length; index++) {
            labels[index] = LogicGateCalculator.Gate.values()[index].getLabel();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gateSpinner.setAdapter(adapter);
        gateSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedGate = LogicGateCalculator.Gate.values()[position];
                updateGate();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        inputA.setOnCheckedChangeListener((button, checked) -> updateLiveOutput());
        inputB.setOnCheckedChangeListener((button, checked) -> updateLiveOutput());
        findViewById(R.id.logicBack).setOnClickListener(view -> finish());
        findViewById(R.id.logicReset).setOnClickListener(view -> resetInputs());
        findViewById(R.id.logicSaveCalculation).setOnClickListener(view -> saveCalculation());
    }

    private void updateGate() {
        boolean needsSecondInput = selectedGate.getInputCount() == 2;
        inputB.setVisibility(needsSecondInput ? View.VISIBLE : View.GONE);
        inputBLabel.setVisibility(needsSecondInput ? View.VISIBLE : View.GONE);
        inputBValue.setVisibility(needsSecondInput ? View.VISIBLE : View.GONE);
        expressionText.setText(selectedGate.getExpression());
        renderTruthTable();
        updateLiveOutput();
    }

    private void updateLiveOutput() {
        boolean a = inputA.isChecked();
        boolean b = inputB.isChecked();
        boolean output = LogicGateCalculator.evaluate(selectedGate, a, b);
        inputAValue.setText(LogicGateCalculator.bit(a));
        inputBValue.setText(LogicGateCalculator.bit(b));
        liveOutput.setText(LogicGateCalculator.bit(output));
        liveOutput.setBackgroundResource(output
                ? R.drawable.bg_logic_output_set : R.drawable.bg_logic_output_clear);
    }

    private void renderTruthTable() {
        truthTable.removeAllViews();
        addTableRow(getString(R.string.gate_table_a), getString(R.string.gate_table_b),
                getString(R.string.gate_table_output), true);
        List<LogicGateCalculator.TruthRow> rows =
                LogicGateCalculator.truthTable(selectedGate);
        for (LogicGateCalculator.TruthRow row : rows) {
            addTableRow(LogicGateCalculator.bit(row.getInputA()),
                    selectedGate.getInputCount() == 1
                            ? "—" : LogicGateCalculator.bit(row.getInputB()),
                    LogicGateCalculator.bit(row.getOutput()), false);
        }
    }

    private void addTableRow(String a, String b, String output, boolean header) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.spacing_small);
        row.setPadding(0, verticalPadding, 0, verticalPadding);
        addTableCell(row, a, header);
        addTableCell(row, b, header);
        addTableCell(row, output, header);
        truthTable.addView(row);
        if (header) {
            View divider = new View(this);
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.card_outline));
            truthTable.addView(divider, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
        }
    }

    private void addTableCell(LinearLayout row, String value, boolean header) {
        TextView cell = new TextView(this);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        cell.setGravity(android.view.Gravity.CENTER);
        cell.setText(value);
        cell.setTextColor(ContextCompat.getColor(this,
                header ? R.color.text_secondary : R.color.text_primary));
        cell.setTextSize(header ? 12 : 16);
        if (header) {
            cell.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        row.addView(cell);
    }

    private void resetInputs() {
        inputA.setChecked(false);
        inputB.setChecked(false);
        updateLiveOutput();
    }

    private void saveCalculation() {
        boolean a = inputA.isChecked();
        boolean b = inputB.isChecked();
        boolean result = LogicGateCalculator.evaluate(selectedGate, a, b);
        String entry = "GATE " + selectedGate.getLabel() + ": A="
                + LogicGateCalculator.bit(a);
        if (selectedGate.getInputCount() == 2) {
            entry += ", B=" + LogicGateCalculator.bit(b);
        }
        entry += " → Y=" + LogicGateCalculator.bit(result);
        CalculationHistory.add(this, entry);
        Toast.makeText(this, R.string.gate_saved, Toast.LENGTH_SHORT).show();
    }
}
