package com.example.cscalc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class CalculationHistory {
    private static final String PREFERENCES = "calculation_history";
    private static final String ENTRIES_KEY = "entries";
    private static final int MAX_ENTRIES = 50;

    private CalculationHistory() {
    }

    public static void add(Context context, String entry) {
        List<String> entries = getRecent(context, MAX_ENTRIES);
        entries.add(0, entry);
        JSONArray array = new JSONArray();
        for (int index = 0; index < Math.min(entries.size(), MAX_ENTRIES); index++) {
            array.put(entries.get(index));
        }
        preferences(context).edit().putString(ENTRIES_KEY, array.toString()).apply();
    }

    public static List<String> getRecent(Context context, int limit) {
        List<String> entries = new ArrayList<>();
        if (limit <= 0) {
            return entries;
        }
        String stored = preferences(context).getString(ENTRIES_KEY, "[]");
        try {
            JSONArray array = new JSONArray(stored);
            for (int index = 0; index < array.length() && entries.size() < limit; index++) {
                entries.add(array.getString(index));
            }
        } catch (JSONException exception) {
            throw new IllegalStateException("Saved calculation history is invalid", exception);
        }
        return entries;
    }

    public static void clear(Context context) {
        preferences(context).edit().remove(ENTRIES_KEY).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
