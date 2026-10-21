package com.example.cscalc.history;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

/**
 * Shared repository service for saving, reading, searching, and deleting
 * calculation records across all modules in CS Student Toolkit.
 * Ensures all database operations run off the main UI thread.
 */
public class HistoryRepository {

    private static final String TAG = "HistoryRepository";
    private static volatile HistoryRepository INSTANCE;

    public static final String MODULE_ALL = "All Modules";
    public static final String MODULE_CONVERTER = "Number System Converter";
    public static final String MODULE_CALCULATOR = "Number System Calculator";
    public static final String MODULE_FLAGS = "Microprocessor Flag Register";
    public static final String MODULE_LOGIC = "Logic Gates";
    public static final String MODULE_BOOLEAN = "Boolean Algebra";
    public static final String MODULE_BINARY = "Binary Arithmetic";
    public static final String MODULE_BITWISE = "Bitwise Operations";
    public static final String MODULE_MEMORY = "Memory & Data Units";

    private final CalculationRecordDao recordDao;
    private final ExecutorService executor;
    private final Executor callbackExecutor;
    // Lazy to avoid Looper.getMainLooper() crash in pure-JVM unit tests.
    private volatile Handler mainHandler;

    public interface Callback<T> {
        void onSuccess(T result);
        default void onError(Exception exception) {
            Log.e(TAG, "History repository operation error", exception);
        }
    }

    /** Returns the main-thread Handler, creating it on first use. */
    private Handler getMainHandler() {
        if (mainHandler == null) {
            synchronized (this) {
                if (mainHandler == null) {
                    mainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mainHandler;
    }

    public HistoryRepository(CalculationRecordDao recordDao) {
        this.recordDao = recordDao;
        this.callbackExecutor = callback -> getMainHandler().post(callback);
        this.executor = Executors.newSingleThreadExecutor();
        // mainHandler is created lazily on first async call.
    }

    HistoryRepository(CalculationRecordDao recordDao, Executor callbackExecutor) {
        this.recordDao = recordDao;
        this.callbackExecutor = callbackExecutor;
        this.executor = Executors.newSingleThreadExecutor();
        // mainHandler is created lazily on first async call.
    }

    public static HistoryRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (HistoryRepository.class) {
                if (INSTANCE == null) {
                    AppDatabase database = AppDatabase.getInstance(context);
                    INSTANCE = new HistoryRepository(database.calculationRecordDao());
                }
            }
        }
        return INSTANCE;
    }

    public void record(@NonNull String moduleName,
                       @NonNull String operation,
                       @NonNull String inputExpression,
                       @NonNull String result,
                       @NonNull String configuration,
                       @NonNull String extraDetails) {
        CalculationRecord record = new CalculationRecord(
                moduleName, operation, inputExpression, result, configuration,
                System.currentTimeMillis(), extraDetails);
        insert(record, null);
    }

    public void insert(@NonNull CalculationRecord record) {
        insert(record, null);
    }

    public void insert(@NonNull CalculationRecord record, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                long id = recordDao.insert(record);
                record.setId(id);
                if (callback != null) {
                    callbackExecutor.execute(() -> callback.onSuccess(id));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting record", e);
                if (callback != null) {
                    callbackExecutor.execute(() -> callback.onError(e));
                }
            }
        });
    }

    public void getAll(@NonNull Callback<List<CalculationRecord>> callback) {
        executor.execute(() -> {
            try {
                List<CalculationRecord> records = recordDao.getAll();
                callbackExecutor.execute(() -> callback.onSuccess(records));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching all records", e);
                callbackExecutor.execute(() -> callback.onError(e));
            }
        });
    }

    public void getByModule(String module, @NonNull Callback<List<CalculationRecord>> callback) {
        executor.execute(() -> {
            try {
                List<CalculationRecord> records = recordDao.getByModule(module);
                callbackExecutor.execute(() -> callback.onSuccess(records));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching records by module", e);
                callbackExecutor.execute(() -> callback.onError(e));
            }
        });
    }

    public void search(String query, String module, @NonNull Callback<List<CalculationRecord>> callback) {
        executor.execute(() -> {
            try {
                String cleanQuery = query == null ? "" : query.trim();
                String cleanModule = (module == null || "All".equalsIgnoreCase(module)) ? null : module;
                List<CalculationRecord> records = recordDao.search(cleanQuery, cleanModule);
                callbackExecutor.execute(() -> callback.onSuccess(records));
            } catch (Exception e) {
                Log.e(TAG, "Error searching records", e);
                callbackExecutor.execute(() -> callback.onError(e));
            }
        });
    }

    public void getRecent(int limit, @NonNull Callback<List<CalculationRecord>> callback) {
        executor.execute(() -> {
            try {
                List<CalculationRecord> records = recordDao.getRecent(limit);
                callbackExecutor.execute(() -> callback.onSuccess(records));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching recent records", e);
                callbackExecutor.execute(() -> callback.onError(e));
            }
        });
    }

    public void delete(@NonNull CalculationRecord record, Callback<Integer> callback) {
        deleteById(record.getId(), callback);
    }

    public void deleteById(long id, Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int deleted = recordDao.deleteById(id);
                if (callback != null) {
                    callbackExecutor.execute(() -> callback.onSuccess(deleted));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting record by id", e);
                if (callback != null) {
                    callbackExecutor.execute(() -> callback.onError(e));
                }
            }
        });
    }

    public void clearAll(Callback<Void> callback) {
        executor.execute(() -> {
            try {
                recordDao.clearAll();
                if (callback != null) {
                    callbackExecutor.execute(() -> callback.onSuccess(null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error clearing history", e);
                if (callback != null) {
                    callbackExecutor.execute(() -> callback.onError(e));
                }
            }
        });
    }

    // Synchronous methods for testing or background worker execution
    public long insertSync(CalculationRecord record) {
        return recordDao.insert(record);
    }

    public List<CalculationRecord> getAllSync() {
        return recordDao.getAll();
    }

    public List<CalculationRecord> searchSync(String query, String module) {
        String cleanQuery = query == null ? "" : query.trim();
        String cleanModule = (module == null || "All".equalsIgnoreCase(module)) ? null : module;
        return recordDao.search(cleanQuery, cleanModule);
    }

    public int deleteByIdSync(long id) {
        return recordDao.deleteById(id);
    }

    public void clearAllSync() {
        recordDao.clearAll();
    }
}
