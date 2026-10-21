package com.example.cscalc;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

public class UnavailableActivity extends AppCompatActivity {
    public static final String EXTRA_MODULE_NAME = "module_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unavailable);
        SystemBarInsets.apply(findViewById(R.id.unavailableRoot));

        String moduleName = getIntent().getStringExtra(EXTRA_MODULE_NAME);
        if (moduleName == null || moduleName.trim().isEmpty()) {
            moduleName = getString(R.string.tool_unavailable_title);
        }
        ((TextView) findViewById(R.id.unavailableModuleName)).setText(moduleName);
        findViewById(R.id.unavailableBack).setOnClickListener(view -> finish());
        findViewById(R.id.unavailableHome).setOnClickListener(view -> finish());
    }
}
