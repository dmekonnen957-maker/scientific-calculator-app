package com.example.cscalc;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cscalc.utils.CalculationHistory;
import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private LinearLayout historyList;
    private TextView emptyMessage;
    private TextView clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SystemBarInsets.apply(findViewById(R.id.historyRoot));
        historyList = findViewById(R.id.historyList);
        emptyMessage = findViewById(R.id.historyEmpty);
        clearButton = findViewById(R.id.historyClear);

        findViewById(R.id.historyBack).setOnClickListener(view -> finish());
        clearButton.setOnClickListener(view -> {
            CalculationHistory.clear(this);
            renderHistory();
        });
        renderHistory();
    }

    private void renderHistory() {
        historyList.removeAllViews();
        List<String> entries = CalculationHistory.getRecent(this, 50);
        emptyMessage.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        clearButton.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
        historyList.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
        for (String entry : entries) {
            TextView item = new TextView(this);
            item.setText(entry);
            item.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            item.setTextSize(14);
            item.setPadding(getResources().getDimensionPixelSize(R.dimen.spacing_medium),
                    getResources().getDimensionPixelSize(R.dimen.spacing_medium),
                    getResources().getDimensionPixelSize(R.dimen.spacing_medium),
                    getResources().getDimensionPixelSize(R.dimen.spacing_medium));
            item.setBackgroundResource(R.drawable.bg_tool_card);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.spacing_small);
            historyList.addView(item, params);
        }
    }
}
