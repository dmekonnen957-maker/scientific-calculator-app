package com.example.cscalc.history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CalculationRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CalculationRecord record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CalculationRecord> records);

    @Update
    int update(CalculationRecord record);

    @Delete
    int delete(CalculationRecord record);

    @Query("DELETE FROM calculation_records WHERE id = :id")
    int deleteById(long id);

    @Query("DELETE FROM calculation_records")
    void clearAll();

    @Query("SELECT * FROM calculation_records ORDER BY timestamp DESC, id DESC")
    List<CalculationRecord> getAll();

    @Query("SELECT * FROM calculation_records WHERE moduleName = :module ORDER BY timestamp DESC, id DESC")
    List<CalculationRecord> getByModule(String module);

    @Query("SELECT * FROM calculation_records WHERE (:module IS NULL OR :module = '' OR :module = 'All' OR moduleName = :module) " +
            "AND (inputExpression LIKE '%' || :query || '%' " +
            "OR result LIKE '%' || :query || '%' " +
            "OR operation LIKE '%' || :query || '%' " +
            "OR configuration LIKE '%' || :query || '%' " +
            "OR extraDetails LIKE '%' || :query || '%') " +
            "ORDER BY timestamp DESC, id DESC")
    List<CalculationRecord> search(String query, String module);

    @Query("SELECT * FROM calculation_records ORDER BY timestamp DESC, id DESC LIMIT :limit")
    List<CalculationRecord> getRecent(int limit);

    @Query("SELECT * FROM calculation_records WHERE id = :id LIMIT 1")
    CalculationRecord getById(long id);

    @Query("SELECT COUNT(*) FROM calculation_records")
    int count();
}
