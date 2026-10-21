package com.example.cscalc.history;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Entity representing a calculation record across all CS Student Toolkit modules.
 */
@Entity(tableName = "calculation_records")
public class CalculationRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String moduleName;

    @NonNull
    private String operation;

    @NonNull
    private String inputExpression;

    @NonNull
    private String result;

    @NonNull
    private String configuration; // Base, bit width, processor flags, etc.

    private long timestamp;

    @NonNull
    private String extraDetails; // Steps, additional operands, explanations to reproduce

    public CalculationRecord() {
        this.moduleName = "";
        this.operation = "";
        this.inputExpression = "";
        this.result = "";
        this.configuration = "";
        this.timestamp = System.currentTimeMillis();
        this.extraDetails = "";
    }

    @Ignore
    public CalculationRecord(@NonNull String moduleName,
                             @NonNull String operation,
                             @NonNull String inputExpression,
                             @NonNull String result,
                             @NonNull String configuration,
                             long timestamp,
                             @NonNull String extraDetails) {
        this.moduleName = moduleName != null ? moduleName : "";
        this.operation = operation != null ? operation : "";
        this.inputExpression = inputExpression != null ? inputExpression : "";
        this.result = result != null ? result : "";
        this.configuration = configuration != null ? configuration : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.extraDetails = extraDetails != null ? extraDetails : "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(@NonNull String moduleName) {
        this.moduleName = moduleName;
    }

    @NonNull
    public String getOperation() {
        return operation;
    }

    public void setOperation(@NonNull String operation) {
        this.operation = operation;
    }

    @NonNull
    public String getInputExpression() {
        return inputExpression;
    }

    public void setInputExpression(@NonNull String inputExpression) {
        this.inputExpression = inputExpression;
    }

    @NonNull
    public String getResult() {
        return result;
    }

    public void setResult(@NonNull String result) {
        this.result = result;
    }

    @NonNull
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(@NonNull String configuration) {
        this.configuration = configuration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    public String getExtraDetails() {
        return extraDetails;
    }

    public void setExtraDetails(@NonNull String extraDetails) {
        this.extraDetails = extraDetails;
    }

    public String getSummary() {
        if (!operation.isEmpty() && !inputExpression.isEmpty()) {
            return operation + ": " + inputExpression + " → " + result;
        } else if (!inputExpression.isEmpty()) {
            return inputExpression + " → " + result;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculationRecord that = (CalculationRecord) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
