package com.example.cscalc;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cscalc.history.CalculationRecord;
import com.example.cscalc.history.HistoryRepository;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends ToolkitActivity {

    private EditText searchInput;
    private Spinner moduleFilterSpinner;
    private TextView countLabel;
    private TextView emptyMessage;
    private TextView clearButton;
    private LinearLayout historyList;

    private HistoryRepository repository;
    private final String[] moduleOptions = new String[]{
            HistoryRepository.MODULE_ALL,
            HistoryRepository.MODULE_CONVERTER,
            HistoryRepository.MODULE_CALCULATOR,
            HistoryRepository.MODULE_LOGIC,
            HistoryRepository.MODULE_BOOLEAN,
            HistoryRepository.MODULE_BINARY,
            HistoryRepository.MODULE_BITWISE,
            HistoryRepository.MODULE_MEMORY,
            HistoryRepository.MODULE_FLAGS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SystemBarInsets.apply(findViewById(R.id.historyRoot));

        repository = HistoryRepository.getInstance(this);

        searchInput = findViewById(R.id.historySearchInput);
        moduleFilterSpinner = findViewById(R.id.historyModuleFilter);
        countLabel = findViewById(R.id.historyCountLabel);
        emptyMessage = findViewById(R.id.historyEmpty);
        clearButton = findViewById(R.id.historyClear);
        historyList = findViewById(R.id.historyList);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, moduleOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleFilterSpinner.setAdapter(filterAdapter);

        findViewById(R.id.historyBack).setOnClickListener(view -> finish());

        clearButton.setOnClickListener(view -> confirmClearAll());

        moduleFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadHistory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadHistory();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        String query = searchInput.getText() != null ? searchInput.getText().toString().trim() : "";
        String selectedModule = moduleFilterSpinner.getSelectedItem() != null
                ? moduleFilterSpinner.getSelectedItem().toString() : HistoryRepository.MODULE_ALL;

        repository.search(query, selectedModule, new HistoryRepository.Callback<List<CalculationRecord>>() {
            @Override
            public void onSuccess(List<CalculationRecord> records) {
                renderRecords(records);
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(HistoryActivity.this, "Unable to load history: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderRecords(List<CalculationRecord> records) {
        historyList.removeAllViews();
        boolean isEmpty = records.isEmpty();
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        historyList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        clearButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            countLabel.setText("No calculations found.");
            return;
        }

        countLabel.setText(String.format(Locale.getDefault(), "Showing %d calculation%s (newest first)",
                records.size(), records.size() == 1 ? "" : "s"));

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault());

        for (CalculationRecord record : records) {
            View card = inflater.inflate(R.layout.item_history_record, historyList, false);

            TextView badge = card.findViewById(R.id.recordModuleBadge);
            TextView timestamp = card.findViewById(R.id.recordTimestamp);
            TextView operationConfig = card.findViewById(R.id.recordOperationConfig);
            TextView input = card.findViewById(R.id.recordInput);
            TextView result = card.findViewById(R.id.recordResult);
            View deleteBtn = card.findViewById(R.id.recordDeleteButton);
            View detailsBtn = card.findViewById(R.id.recordDetailsButton);
            View reuseBtn = card.findViewById(R.id.recordReuseButton);

            badge.setText(record.getModuleName());
            timestamp.setText(dateFormat.format(new Date(record.getTimestamp())));

            StringBuilder opConfigSb = new StringBuilder();
            if (!record.getOperation().isEmpty()) {
                opConfigSb.append(record.getOperation());
            }
            if (!record.getConfiguration().isEmpty()) {
                if (opConfigSb.length() > 0) opConfigSb.append(" · ");
                opConfigSb.append(record.getConfiguration());
            }
            operationConfig.setText(opConfigSb.toString());

            input.setText(record.getInputExpression());
            result.setText(record.getResult());

            deleteBtn.setOnClickListener(v -> confirmDeleteRecord(record));
            detailsBtn.setOnClickListener(v -> showRecordDetails(record));
            reuseBtn.setOnClickListener(v -> reuseRecord(record));

            card.setOnClickListener(v -> showRecordDetails(record));

            historyList.addView(card);
        }
    }

    private void showRecordDetails(CalculationRecord record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(record.getTimestamp()));

        StringBuilder message = new StringBuilder();
        message.append("Record ID: #").append(record.getId()).append("\n\n");
        message.append("Module: ").append(record.getModuleName()).append("\n");
        message.append("Operation: ").append(record.getOperation()).append("\n");
        if (!record.getConfiguration().isEmpty()) {
            message.append("Configuration: ").append(record.getConfiguration()).append("\n");
        }
        message.append("Timestamp: ").append(formattedDate).append("\n\n");
        message.append("Input:\n").append(record.getInputExpression()).append("\n\n");
        message.append("Result:\n").append(record.getResult());

        if (!record.getExtraDetails().isEmpty() && !record.getExtraDetails().equals(record.getInputExpression())) {
            message.append("\n\nDetails / Reproduction:\n").append(record.getExtraDetails());
        }

        new AlertDialog.Builder(this)
                .setTitle(record.getModuleName())
                .setMessage(message.toString())
                .setPositiveButton("Reuse as Input", (dialog, which) -> reuseRecord(record))
                .setNeutralButton("Delete", (dialog, which) -> confirmDeleteRecord(record))
                .setNegativeButton("Close", null)
                .show();
    }

    private void reuseRecord(CalculationRecord record) {
        String module = record.getModuleName();
        Intent intent;

        if (HistoryRepository.MODULE_CONVERTER.equals(module)) {
            intent = new Intent(this, NumberConverterActivity.class);
            intent.putExtra("REUSE_INPUT", record.getInputExpression());
            intent.putExtra("REUSE_BASE", record.getConfiguration().replace("Source: ", "").trim());
        } else if (HistoryRepository.MODULE_CALCULATOR.equals(module)) {
            intent = new Intent(this, MultiBaseCalculatorActivity.class);
            intent.putExtra("REUSE_EXPRESSION", record.getInputExpression());
            intent.putExtra("REUSE_BASE", record.getConfiguration().replace("Base: ", "").trim());
        } else if (HistoryRepository.MODULE_FLAGS.equals(module)) {
            String[] operands = record.getInputExpression().trim().split("\\s+");
            if (operands.length != 3) {
                Toast.makeText(this, "This flag-register record cannot be reused.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String[] configuration = record.getConfiguration().split(",", 2);
            intent = new Intent(this, FlagRegisterActivity.class);
            intent.putExtra("REUSE_PROCESSOR", configuration[0].trim());
            intent.putExtra("REUSE_WIDTH",
                    record.getConfiguration().contains("16-bit") ? "16" : "8");
            intent.putExtra("REUSE_OP", record.getOperation());
            intent.putExtra("REUSE_A", operands[0]);
            intent.putExtra("REUSE_B", operands[2]);
        } else if (HistoryRepository.MODULE_LOGIC.equals(module)) {
            intent = new Intent(this, LogicGatesActivity.class);
            intent.putExtra("REUSE_GATE", record.getOperation());
            intent.putExtra("REUSE_INPUTS", record.getInputExpression());
        } else if (HistoryRepository.MODULE_BOOLEAN.equals(module)) {
            intent = new Intent(this, BooleanAlgebraActivity.class);
            intent.putExtra("REUSE_EXPRESSION", record.getInputExpression());
        } else if (HistoryRepository.MODULE_BINARY.equals(module)) {
            intent = new Intent(this, BinaryArithmeticActivity.class);
            intent.putExtra("REUSE_OP", record.getOperation());
            intent.putExtra("REUSE_EXPRESSION", record.getInputExpression());
        } else if (HistoryRepository.MODULE_BITWISE.equals(module)) {
            intent = new Intent(this, BitwiseOperationsActivity.class);
            intent.putExtra("REUSE_OP", record.getOperation());
            intent.putExtra("REUSE_INPUT", record.getInputExpression());
            intent.putExtra("REUSE_WIDTH", record.getConfiguration());
        } else if (HistoryRepository.MODULE_MEMORY.equals(module)) {
            intent = new Intent(this, MemoryUnitsActivity.class);
            intent.putExtra("REUSE_INPUT", record.getInputExpression());
            intent.putExtra("REUSE_CONFIG", record.getConfiguration());
        } else {
            Toast.makeText(this, "Module reuse not supported for: " + module, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);
        Toast.makeText(this, "Loaded calculation into " + module, Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteRecord(CalculationRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Delete calculation #" + record.getId() + " (" + record.getModuleName() + ") from history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.delete(record, new HistoryRepository.Callback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            Toast.makeText(HistoryActivity.this, "Record deleted", Toast.LENGTH_SHORT).show();
                            loadHistory();
                        }

                        @Override
                        public void onError(Exception exception) {
                            Toast.makeText(HistoryActivity.this, "Failed to delete: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all saved calculations? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    repository.clearAll(new HistoryRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(HistoryActivity.this, "Calculation history cleared", Toast.LENGTH_SHORT).show();
                            loadHistory();
                        }

                        @Override
                        public void onError(Exception exception) {
                            Toast.makeText(HistoryActivity.this, "Failed to clear history: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
