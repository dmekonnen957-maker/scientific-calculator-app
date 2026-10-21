package com.example.cscalc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.cscalc.history.CalculationRecord;
import com.example.cscalc.history.HistoryRepository;

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

    public static void record(Context context, @NonNull CalculationRecord record) {
        // Save to Room SQLite via shared HistoryRepository
        HistoryRepository.getInstance(context).insert(record);

        // Keep SharedPreferences legacy list in sync for fast widget/fallback reads
        addLegacy(context, record.getSummary());
    }

    public static void record(Context context,
                              String moduleName,
                              String operation,
                              String inputExpression,
                              String result,
                              String configuration,
                              String extraDetails) {
        CalculationRecord record = new CalculationRecord(
                moduleName, operation, inputExpression, result, configuration,
                System.currentTimeMillis(), extraDetails);
        record(context, record);
    }

    public static void add(Context context, String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return;
        }

        // Parse legacy string entry into structured CalculationRecord
        CalculationRecord record = parseRecordFromLegacyString(entry);
        HistoryRepository.getInstance(context).insert(record);

        addLegacy(context, entry);
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
        HistoryRepository.getInstance(context).clearAll(null);
    }

    private static void addLegacy(Context context, String entry) {
        List<String> entries = getRecent(context, MAX_ENTRIES);
        entries.add(0, entry);
        JSONArray array = new JSONArray();
        for (int index = 0; index < Math.min(entries.size(), MAX_ENTRIES); index++) {
            array.put(entries.get(index));
        }
        preferences(context).edit().putString(ENTRIES_KEY, array.toString()).apply();
    }

    private static CalculationRecord parseRecordFromLegacyString(String entry) {
        String module;
        String op = "Calculation";
        String input = entry;
        String result = entry;
        String config = "";

        if (entry.startsWith("CALC ")) {
            module = HistoryRepository.MODULE_CALCULATOR;
            op = "Evaluate";
            int colonIdx = entry.indexOf(':');
            int eqIdx = entry.indexOf('=');
            if (colonIdx != -1 && eqIdx != -1) {
                config = entry.substring(5, colonIdx).trim();
                input = entry.substring(colonIdx + 1, eqIdx).trim();
                result = entry.substring(eqIdx + 1).trim();
            }
        } else if (entry.startsWith("GATE ")) {
            module = HistoryRepository.MODULE_LOGIC;
            int colonIdx = entry.indexOf(':');
            int arrowIdx = entry.indexOf('→');
            if (colonIdx != -1) {
                op = entry.substring(5, colonIdx).trim();
                if (arrowIdx != -1) {
                    input = entry.substring(colonIdx + 1, arrowIdx).trim();
                    result = entry.substring(arrowIdx + 1).trim();
                }
            }
        } else if (entry.startsWith("BOOLEAN ")) {
            module = HistoryRepository.MODULE_BOOLEAN;
            op = entry.contains("SIMPLIFY") ? "Simplify" : "Evaluate";
        } else if (entry.startsWith("BIN ")) {
            module = HistoryRepository.MODULE_BINARY;
            op = "Arithmetic";
            int eqIdx = entry.indexOf('=');
            if (eqIdx != -1) {
                input = entry.substring(4, eqIdx).trim();
                result = entry.substring(eqIdx + 1).trim();
            }
        } else if (entry.startsWith("BITWISE ")) {
            module = HistoryRepository.MODULE_BITWISE;
            op = "Bitwise";
            int bracketIdx = entry.indexOf(']');
            int eqIdx = entry.indexOf('=');
            if (bracketIdx != -1 && eqIdx != -1) {
                config = entry.substring(entry.indexOf('[') + 1, bracketIdx).trim();
                input = entry.substring(bracketIdx + 1, eqIdx).trim();
                result = entry.substring(eqIdx + 1).trim();
            }
        } else if (entry.startsWith("MEMORY: ")) {
            module = HistoryRepository.MODULE_MEMORY;
            op = "Unit Conversion";
            int eqIdx = entry.indexOf('=');
            if (eqIdx != -1) {
                input = entry.substring(8, eqIdx).trim();
                result = entry.substring(eqIdx + 1).trim();
            }
        } else {
            module = HistoryRepository.MODULE_CONVERTER;
            op = "Base Conversion";
            int arrowIdx = entry.indexOf('→');
            if (arrowIdx != -1) {
                input = entry.substring(0, arrowIdx).trim();
                result = entry.substring(arrowIdx + 1).trim();
            }
        }

        return new CalculationRecord(
                module, op, input, result, config, System.currentTimeMillis(), entry
        );
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
