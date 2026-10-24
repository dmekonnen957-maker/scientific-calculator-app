package com.example.cscalc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
        List<Tool> tools = getTools();
        LinearLayout row = null;
        for (int index = 0; index < tools.size(); index++) {
            if (index % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setWeightSum(2f);
                container.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            addToolkitCard(row, tools.get(index));
        }
        if (tools.size() % 2 != 0 && row != null) {
            View spacer = new View(this);
            row.addView(spacer, cardLayoutParams());
        }
    }

    private List<Tool> getTools() {
        List<Tool> tools = new ArrayList<>();
        tools.add(new Tool("Number System Converter", "Convert BIN, OCT, DEC and HEX",
                "↔", NumberConverterActivity.class));
        tools.add(new Tool("Number System Calculator", "Arithmetic in four number bases",
                "±", MultiBaseCalculatorActivity.class));
        tools.add(new Tool("Microprocessor Flag Register",
                "Explore 8085 and 8086 status flags", "⚑", FlagRegisterActivity.class));
        tools.add(new Tool("Logic Gates", "Interactive gates and truth tables",
                "AND", LogicGatesActivity.class));
        tools.add(new Tool("Boolean Algebra", "Evaluate, simplify and build truth tables",
                "B", BooleanAlgebraActivity.class));
        tools.add(new Tool("Binary Arithmetic", "Binary arithmetic with worked steps",
                "01", BinaryArithmeticActivity.class));
        tools.add(new Tool("Bitwise Operations", "Fixed-width bitwise operations",
                "&", BitwiseOperationsActivity.class));
        tools.add(new Tool("Memory & Data Units", "Convert SI and IEC data units",
                "KB", MemoryUnitsActivity.class));
        tools.add(new Tool("Calculation History", "Search, review and manage calculations",
                "↶", HistoryActivity.class));
        return tools;
    }

    private void addToolkitCard(LinearLayout row, Tool tool) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_tool_card, row, false);
        ((TextView) card.findViewById(R.id.toolIcon)).setText(tool.icon);
        ((TextView) card.findViewById(R.id.toolTitle)).setText(tool.title);
        ((TextView) card.findViewById(R.id.toolSubtitle)).setText(tool.subtitle);
        card.setContentDescription(getString(R.string.open_toolkit_accessibility, tool.title));
        card.setOnClickListener(view -> onToolkitTapped(tool));
        row.addView(card, cardLayoutParams());
    }

    private LinearLayout.LayoutParams cardLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, getResources().getDimensionPixelSize(R.dimen.tool_card_height), 1f);
        int margin = getResources().getDimensionPixelSize(R.dimen.spacing_xsmall);
        params.setMargins(margin, margin, margin, margin);
        return params;
    }

    private void onToolkitTapped(Tool tool) {
        if (launchingToolkit) {
            return;
        }
        launchingToolkit = true;
        launch(new Intent(this, tool.destination));
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
        final String icon;
        final Class<? extends AppCompatActivity> destination;

        Tool(String title, String subtitle, String icon,
             Class<? extends AppCompatActivity> destination) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
            this.destination = destination;
        }
    }
}
