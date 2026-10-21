package com.example.cscalc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean launchingToolkit;
    private boolean wideToolkitTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.apply(findViewById(R.id.dashboardRoot));

        findViewById(R.id.settingsButton).setOnClickListener(
                view -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.historyNav).setOnClickListener(
                view -> launch(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.toolsNav).setOnClickListener(view -> {
            // The dashboard is already selected.
        });

        populateToolCards();
        populateRecentCalculations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        launchingToolkit = false;
        if (findViewById(R.id.recentList) != null) {
            populateRecentCalculations();
        }
    }

    private void populateToolCards() {
        LinearLayout container = findViewById(R.id.toolsContainer);
        container.removeAllViews();
        wideToolkitTable = getResources().getConfiguration().screenWidthDp >= 560;
        List<Tool> tools = getTools();
        addToolkitHeader(container);
        for (int index = 0; index < tools.size(); index++) {
            addToolkitRow(container, tools.get(index), index + 1);
            View divider = new View(this);
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.card_outline));
            container.addView(divider, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
        }
    }

    private List<Tool> getTools() {
        List<Tool> tools = new ArrayList<>();
        tools.add(new Tool("Number System Converter", "Convert BIN, OCT, DEC and HEX", "converter"));
        tools.add(new Tool("Number System Calculator", "Arithmetic in four number bases", "calculator"));
        tools.add(new Tool("Microprocessor Flag Register",
                "Explore 8085 and 8086 status flags", "flags"));
        tools.add(new Tool("Logic Gates", "Interactive gates and truth tables", "logic"));
        tools.add(new Tool("Boolean Algebra", "Evaluate, simplify and build truth tables", "boolean"));
        tools.add(new Tool("Binary Arithmetic", "Binary arithmetic with worked steps", "binary"));
        tools.add(new Tool("Bitwise Operations", "Fixed-width bitwise operations", "bitwise"));
        tools.add(new Tool("Memory & Data Units", "Convert SI and IEC data units", "memory"));
        tools.add(new Tool("Calculation History", "Search, review and manage calculations", "history"));
        return tools;
    }

    private void addToolkitHeader(LinearLayout container) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_small),
                0, getResources().getDimensionPixelSize(R.dimen.spacing_small));
        addCell(header, "#", 0.55f, wideToolkitTable ? 0 : 36, true);
        if (wideToolkitTable) {
            addCell(header, getString(R.string.toolkit_column_name), 1.25f, 0, true);
            addCell(header, getString(R.string.toolkit_column_description), 1.8f, 0, true);
        } else {
            addCell(header, getString(R.string.toolkit_column_name_description), 1f, 0, true);
        }
        addCell(header, getString(R.string.toolkit_column_open), 0.7f, wideToolkitTable ? 58 : 58, true);
        container.addView(header);
        View divider = new View(this);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue));
        container.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
    }

    private void addToolkitRow(LinearLayout container, Tool tool, int number) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setMinimumHeight(getResources().getDimensionPixelSize(
                wideToolkitTable ? R.dimen.toolkit_table_row_height
                        : R.dimen.toolkit_compact_row_height));
        row.setPadding(getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                getResources().getDimensionPixelSize(R.dimen.spacing_xsmall),
                getResources().getDimensionPixelSize(R.dimen.spacing_xsmall));
        row.setBackgroundResource(android.R.drawable.list_selector_background);
        row.setClickable(true);
        row.setFocusable(true);

        addCell(row, String.format(java.util.Locale.ROOT, "%02d", number),
                0.55f, wideToolkitTable ? 0 : 36, false);
        if (wideToolkitTable) {
            addCell(row, tool.title, 1.25f, 0, false);
            addCell(row, tool.subtitle, 1.8f, 0, false);
        } else {
            LinearLayout textColumn = new LinearLayout(this);
            textColumn.setOrientation(LinearLayout.VERTICAL);
            textColumn.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.spacing_small), 0);
            row.addView(textColumn, textParams);
            TextView title = createCell(tool.title, true);
            TextView description = createCell(tool.subtitle, false);
            description.setTextSize(11);
            textColumn.addView(title);
            textColumn.addView(description);
        }
        TextView open = new TextView(this);
        open.setText(R.string.toolkit_open);
        open.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        open.setTextSize(13);
        open.setTypeface(null, android.graphics.Typeface.BOLD);
        open.setGravity(android.view.Gravity.CENTER);
        open.setBackgroundResource(android.R.drawable.btn_default);
        open.setMinHeight(getResources().getDimensionPixelSize(R.dimen.toolkit_action_height));
        open.setContentDescription(getString(R.string.open_toolkit_accessibility, tool.title));
        LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(
                58,
                getResources().getDimensionPixelSize(R.dimen.toolkit_action_height));
        row.addView(open, openParams);

        row.setContentDescription(getString(R.string.open_toolkit_accessibility, tool.title));
        row.setOnClickListener(view -> onToolkitTapped(row, tool));
        open.setOnClickListener(view -> onToolkitTapped(row, tool));
        container.addView(row);
    }

    private void addCell(LinearLayout row, String value, float weight, int width, boolean header) {
        TextView cell = createCell(value, header);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width == 0 ? 0 : width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                width == 0 ? weight : 0);
        if (header) {
            params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.spacing_xsmall), 0);
        }
        row.addView(cell, params);
    }

    private TextView createCell(String value, boolean header) {
        TextView cell = new TextView(this);
        cell.setText(value);
        cell.setTextColor(ContextCompat.getColor(this,
                header ? R.color.text_secondary : R.color.text_primary));
        cell.setTextSize(header ? 11 : 13);
        cell.setMaxLines(header ? 1 : 2);
        if (header || !header && value.length() < 4) {
            cell.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return cell;
    }

    private void onToolkitTapped(View row, Tool tool) {
        if (launchingToolkit) {
            return;
        }
        launchingToolkit = true;
        row.animate().scaleX(0.985f).scaleY(0.985f).setDuration(90)
                .withEndAction(() -> row.animate().scaleX(1f).scaleY(1f).setDuration(110)
                        .withEndAction(() -> openTool(tool)).start()).start();
    }

    private void openTool(Tool tool) {
        if ("converter".equals(tool.destination)) {
            launch(new Intent(this, NumberConverterActivity.class));
        } else if ("flags".equals(tool.destination)) {
            launch(new Intent(this, FlagRegisterActivity.class));
        } else if ("calculator".equals(tool.destination)) {
            launch(new Intent(this, MultiBaseCalculatorActivity.class));
        } else if ("logic".equals(tool.destination)) {
            launch(new Intent(this, LogicGatesActivity.class));
        } else if ("boolean".equals(tool.destination)) {
            launch(new Intent(this, BooleanAlgebraActivity.class));
        } else if ("binary".equals(tool.destination)) {
            launch(new Intent(this, BinaryArithmeticActivity.class));
        } else if ("bitwise".equals(tool.destination)) {
            launch(new Intent(this, BitwiseOperationsActivity.class));
        } else if ("memory".equals(tool.destination)) {
            launch(new Intent(this, MemoryUnitsActivity.class));
        } else if ("history".equals(tool.destination)) {
            launch(new Intent(this, HistoryActivity.class));
        } else {
            Intent intent = new Intent(this, UnavailableActivity.class);
            intent.putExtra(UnavailableActivity.EXTRA_MODULE_NAME, tool.title);
            launch(intent);
        }
    }

    private void launch(Intent intent) {
        try {
            startActivity(intent);
            overridePendingTransition(R.anim.toolkit_enter, R.anim.toolkit_exit);
        } catch (android.content.ActivityNotFoundException exception) {
            launchingToolkit = false;
            Toast.makeText(this, R.string.toolkit_open_failed, Toast.LENGTH_LONG).show();
            android.util.Log.e("MainActivity", "Toolkit destination is not registered", exception);
        }
    }

    private void populateRecentCalculations() {
        LinearLayout recentList = findViewById(R.id.recentList);
        TextView emptyMessage = findViewById(R.id.recentEmpty);
        if (recentList == null || emptyMessage == null) return;

        com.example.cscalc.history.HistoryRepository.getInstance(this).getRecent(2,
                new com.example.cscalc.history.HistoryRepository.Callback<List<com.example.cscalc.history.CalculationRecord>>() {
                    @Override
                    public void onSuccess(List<com.example.cscalc.history.CalculationRecord> records) {
                        recentList.removeAllViews();
                        if (records.isEmpty()) {
                            List<String> legacyRecent = CalculationHistory.getRecent(MainActivity.this, 2);
                            emptyMessage.setVisibility(legacyRecent.isEmpty() ? View.VISIBLE : View.GONE);
                            recentList.setVisibility(legacyRecent.isEmpty() ? View.GONE : View.VISIBLE);
                            for (String item : legacyRecent) {
                                addRecentRow(recentList, item);
                            }
                        } else {
                            emptyMessage.setVisibility(View.GONE);
                            recentList.setVisibility(View.VISIBLE);
                            for (com.example.cscalc.history.CalculationRecord record : records) {
                                addRecentRow(recentList, record.getModuleName() + ": " + record.getInputExpression() + " → " + record.getResult());
                            }
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        List<String> recent = CalculationHistory.getRecent(MainActivity.this, 2);
                        emptyMessage.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
                        recentList.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
                        recentList.removeAllViews();
                        for (String item : recent) {
                            addRecentRow(recentList, item);
                        }
                    }
                });
    }

    private void addRecentRow(LinearLayout container, String text) {
        TextView row = new TextView(this);
        row.setText(text);
        row.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        row.setTextSize(14);
        row.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_small), 0,
                getResources().getDimensionPixelSize(R.dimen.spacing_small));
        container.addView(row);
    }

    private static final class Tool {
        final String title;
        final String subtitle;
        final String destination;

        Tool(String title, String subtitle, String destination) {
            this.title = title;
            this.subtitle = subtitle;
            this.destination = destination;
        }
    }
}
