package com.example.cscalc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.cscalc.utils.SystemBarInsets;
import com.example.cscalc.utils.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SystemBarInsets.apply(findViewById(R.id.settingsRoot));

        findViewById(R.id.settingsBack).setOnClickListener(view -> finish());
        SwitchCompat darkThemeSwitch = findViewById(R.id.darkThemeSwitch);
        darkThemeSwitch.setChecked(ThemeManager.isDarkTheme(this));
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemeManager.setDarkTheme(this, isChecked));
    }
}
