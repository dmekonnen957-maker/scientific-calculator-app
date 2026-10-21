package com.example.cscalc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.apply(findViewById(R.id.dashboardRoot));

        findViewById(R.id.settingsButton).setOnClickListener(
                view -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.historyNav).setOnClickListener(
                view -> startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.toolsNav).setOnClickListener(view -> {
            // The dashboard is already selected.
        });

        populateToolCards();
        populateRecentCalculations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (findViewById(R.id.recentList) != null) {
            populateRecentCalculations();
        }
    }

    private void populateToolCards() {
        LinearLayout container = findViewById(R.id.toolsContainer);
        LayoutInflater inflater = LayoutInflater.from(this);
        List<Tool> tools = Arrays.asList(
                new Tool("Number System Converter", "Convert BIN, OCT, DEC and HEX", "⇄", "converter"),
                new Tool("Number System Calculator", "Work with values in any base", "Σ", "calculator"),
                new Tool("Microprocessor Flag Register", "Explore processor status flags", "⚑", "flags"),
                new Tool("Logic Gates", "Learn and evaluate gate operations", "∧", "logic"),
                new Tool("Boolean Algebra", "Simplify Boolean expressions", "ƒ", "boolean"),
                new Tool("Binary Arithmetic", "Practice binary calculations", "01", "binary"),
                new Tool("Bitwise Operations", "AND, OR, XOR and shifts", "&", "bitwise"),
                new Tool("Memory & Data Units", "Convert bits, bytes and units", "▦", "memory"),
                new Tool("Calculation History", "Review your recent conversions", "↶", "history")
        );

        for (int index = 0; index < tools.size(); index += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (index > 0) {
                rowParams.topMargin = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
            }
            container.addView(row, rowParams);

            addToolCard(inflater, row, tools.get(index), index + 1 == tools.size());
            if (index + 1 < tools.size()) {
                View gap = new View(this);
                row.addView(gap, new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.spacing_medium), 1));
                addToolCard(inflater, row, tools.get(index + 1), false);
            }
        }
    }

    private void addToolCard(LayoutInflater inflater, LinearLayout row, Tool tool, boolean fullWidth) {
        View card = inflater.inflate(R.layout.item_tool_card, row, false);
        ((TextView) card.findViewById(R.id.toolIcon)).setText(tool.icon);
        ((TextView) card.findViewById(R.id.toolTitle)).setText(tool.title);
        ((TextView) card.findViewById(R.id.toolSubtitle)).setText(tool.subtitle);

        LinearLayout.LayoutParams params = fullWidth
                ? new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.tool_card_height))
                : new LinearLayout.LayoutParams(0,
                getResources().getDimensionPixelSize(R.dimen.tool_card_height), 1);
        card.setOnClickListener(view -> openTool(tool));
        row.addView(card, params);
    }

    private void openTool(Tool tool) {
        if ("converter".equals(tool.destination)) {
            startActivity(new Intent(this, NumberConverterActivity.class));
        } else if ("calculator".equals(tool.destination)) {
            startActivity(new Intent(this, MultiBaseCalculatorActivity.class));
        } else if ("logic".equals(tool.destination)) {
            startActivity(new Intent(this, LogicGatesActivity.class));
        } else if ("boolean".equals(tool.destination)) {
            startActivity(new Intent(this, BooleanAlgebraActivity.class));
        } else if ("history".equals(tool.destination)) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else {
            Intent intent = new Intent(this, UnavailableActivity.class);
            intent.putExtra(UnavailableActivity.EXTRA_MODULE_NAME, tool.title);
            startActivity(intent);
        }
    }

    private void populateRecentCalculations() {
        LinearLayout recentList = findViewById(R.id.recentList);
        TextView emptyMessage = findViewById(R.id.recentEmpty);
        recentList.removeAllViews();
        List<String> recent = CalculationHistory.getRecent(this, 2);
        emptyMessage.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
        recentList.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
        for (String item : recent) {
            TextView row = new TextView(this);
            row.setText(item);
            row.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            row.setTextSize(14);
            row.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_small), 0,
                    getResources().getDimensionPixelSize(R.dimen.spacing_small));
            recentList.addView(row);
        }
    }

    private static final class Tool {
        final String title;
        final String subtitle;
        final String icon;
        final String destination;

        Tool(String title, String subtitle, String icon, String destination) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
            this.destination = destination;
        }
    }
}
