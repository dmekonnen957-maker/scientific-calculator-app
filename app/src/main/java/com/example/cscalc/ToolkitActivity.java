package com.example.cscalc;

import androidx.appcompat.app.AppCompatActivity;

/** Shared short close transition for full-screen toolkit destinations. */
public abstract class ToolkitActivity extends AppCompatActivity {
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.toolkit_exit);
    }
}
