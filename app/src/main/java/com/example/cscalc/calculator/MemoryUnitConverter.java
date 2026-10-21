package com.example.cscalc.calculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Educational Memory and Data Units conversion logic.
 * Clearly distinguishes SI decimal prefixes (1000-based) from IEC binary prefixes (1024-based).
 * Pure Java (no Android framework dependencies) to facilitate unit testing.
 */
public final class MemoryUnitConverter {

    public enum Standard {
        BASE("Base Unit"),
        SI_DECIMAL("SI Decimal (1000-based, 10^n)"),
        IEC_BINARY("IEC Binary (1024-based, 2^n)");

        private final String label;

        Standard(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Unit {
        // Base units
        BIT("b", "Bit", Standard.BASE, BigDecimal.valueOf(1), "1 bit"),
        BYTE("B", "Byte", Standard.BASE, BigDecimal.valueOf(8), "8 bits"),

        // SI Decimal Units (powers of 10 / 1000)
        KILOBIT("kb", "Kilobit (SI)", Standard.SI_DECIMAL, BigDecimal.valueOf(1_000L), "10^3 bits = 1,000 bits"),
        MEGABIT("Mb", "Megabit (SI)", Standard.SI_DECIMAL, BigDecimal.valueOf(1_000_000L), "10^6 bits = 1,000,000 bits"),
        GIGABIT("Gb", "Gigabit (SI)", Standard.SI_DECIMAL, BigDecimal.valueOf(1_000_000_000L), "10^9 bits = 1,000,000,000 bits"),
        KILOBYTE("kB", "Kilobyte (SI)", Standard.SI_DECIMAL, BigDecimal.valueOf(8_000L), "10^3 bytes = 1,000 B"),
        MEGABYTE("MB", "Megabyte (SI)", Standard.SI_DECIMAL, BigDecimal.valueOf(8_000_000L), "10^6 bytes = 1,000,000 B"),
        GIGABYTE("GB", "Gigabyte (SI)", Standard.SI_DECIMAL, BigDecimal.valueOf(8_000_000_000L), "10^9 bytes = 1,000,000,000 B"),
        TERABYTE("TB", "Terabyte (SI)", Standard.SI_DECIMAL, new BigDecimal("8000000000000"), "10^12 bytes = 1,000,000,000,000 B"),
        PETABYTE("PB", "Petabyte (SI)", Standard.SI_DECIMAL, new BigDecimal("8000000000000000"), "10^15 bytes = 1,000,000,000,000,000 B"),

        // IEC Binary Units (powers of 2 / 1024)
        KIBIBIT("Kib", "Kibibit (IEC)", Standard.IEC_BINARY, BigDecimal.valueOf(1_024L), "2^10 bits = 1,024 bits"),
        MEBIBIT("Mib", "Mebibit (IEC)", Standard.IEC_BINARY, BigDecimal.valueOf(1_048_576L), "2^20 bits = 1,048,576 bits"),
        GIBIBIT("Gib", "Gibibit (IEC)", Standard.IEC_BINARY, BigDecimal.valueOf(1_073_741_824L), "2^30 bits = 1,073,741,824 bits"),
        KIBIBYTE("KiB", "Kibibyte (IEC)", Standard.IEC_BINARY, BigDecimal.valueOf(8_192L), "2^10 bytes = 1,024 B"),
        MEBIBYTE("MiB", "Mebibyte (IEC)", Standard.IEC_BINARY, BigDecimal.valueOf(8_388_608L), "2^20 bytes = 1,048,576 B"),
        GIBIBYTE("GiB", "Gibibyte (IEC)", Standard.IEC_BINARY, new BigDecimal("8589934592"), "2^30 bytes = 1,073,741,824 B"),
        TEBIBYTE("TiB", "Tebibyte (IEC)", Standard.IEC_BINARY, new BigDecimal("8796093022208"), "2^40 bytes = 1,099,511,627,776 B"),
        PEBIBYTE("PiB", "Pebibyte (IEC)", Standard.IEC_BINARY, new BigDecimal("9007199254740992"), "2^50 bytes = 1,125,899,906,842,624 B");

        private final String symbol;
        private final String name;
        private final Standard standard;
        private final BigDecimal bitsMultiplier;
        private final String definition;

        Unit(String symbol, String name, Standard standard, BigDecimal bitsMultiplier, String definition) {
            this.symbol = symbol;
            this.name = name;
            this.standard = standard;
            this.bitsMultiplier = bitsMultiplier;
            this.definition = definition;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }

        public Standard getStandard() {
            return standard;
        }

        public BigDecimal getBitsMultiplier() {
            return bitsMultiplier;
        }

        public String getDefinition() {
            return definition;
        }
    }

    public static final class ConversionResult {
        private final BigDecimal inputValue;
        private final Unit sourceUnit;
        private final Unit targetUnit;
        private final BigDecimal targetValue;
        private final BigDecimal exactBits;
        private final BigDecimal exactBytes;
        private final String formattedTarget;
        private final List<String> steps;
        private final String distinctionExplanation;
        private final Map<Unit, String> equivalentValues;

        public ConversionResult(BigDecimal inputValue, Unit sourceUnit, Unit targetUnit,
                                BigDecimal targetValue, BigDecimal exactBits, BigDecimal exactBytes,
                                String formattedTarget, List<String> steps,
                                String distinctionExplanation,
                                Map<Unit, String> equivalentValues) {
            this.inputValue = inputValue;
            this.sourceUnit = sourceUnit;
            this.targetUnit = targetUnit;
            this.targetValue = targetValue;
            this.exactBits = exactBits;
            this.exactBytes = exactBytes;
            this.formattedTarget = formattedTarget;
            this.steps = steps;
            this.distinctionExplanation = distinctionExplanation;
            this.equivalentValues = equivalentValues;
        }

        public BigDecimal getInputValue() {
            return inputValue;
        }

        public Unit getSourceUnit() {
            return sourceUnit;
        }

        public Unit getTargetUnit() {
            return targetUnit;
        }

        public BigDecimal getTargetValue() {
            return targetValue;
        }

        public BigDecimal getExactBits() {
            return exactBits;
        }

        public BigDecimal getExactBytes() {
            return exactBytes;
        }

        public String getFormattedTarget() {
            return formattedTarget;
        }

        public List<String> getSteps() {
            return steps;
        }

        public String getDistinctionExplanation() {
            return distinctionExplanation;
        }

        public Map<Unit, String> getEquivalentValues() {
            return equivalentValues;
        }
    }

    public static final class WorkedExample {
        private final String title;
        private final String description;
        private final BigDecimal value;
        private final Unit sourceUnit;
        private final Unit targetUnit;

        public WorkedExample(String title, String description, BigDecimal value, Unit sourceUnit, Unit targetUnit) {
            this.title = title;
            this.description = description;
            this.value = value;
            this.sourceUnit = sourceUnit;
            this.targetUnit = targetUnit;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getValue() {
            return value;
        }

        public Unit getSourceUnit() {
            return sourceUnit;
        }

        public Unit getTargetUnit() {
            return targetUnit;
        }
    }

    private static final List<WorkedExample> WORKED_EXAMPLES = new ArrayList<>();

    static {
        WORKED_EXAMPLES.add(new WorkedExample(
                "Storage Drive Capacity: 1 TB Drive in Windows",
                "Why a 1 TB hard drive shows as ~931 GB in Windows: Drive makers use SI decimal (1 TB = 10^12 bytes), while Windows measures in binary IEC (GiB = 2^30 bytes) but labels it 'GB'.",
                BigDecimal.valueOf(1), Unit.TERABYTE, Unit.GIBIBYTE));

        WORKED_EXAMPLES.add(new WorkedExample(
                "RAM Sizing: 16 GiB Memory Module",
                "RAM addresses are powers of 2. A 16 GiB DDR module contains 17,179,869,184 bytes (17.18 decimal GB).",
                BigDecimal.valueOf(16), Unit.GIBIBYTE, Unit.BYTE));

        WORKED_EXAMPLES.add(new WorkedExample(
                "Network Download: 1 Gbps Gigabit Internet",
                "Network speeds are rated in decimal bits per second (1 Gbps = 1,000 Mbps). In bytes, maximum theoretical speed is 125 MB/s (or 119.21 MiB/s).",
                BigDecimal.valueOf(1), Unit.GIGABIT, Unit.MEGABYTE));

        WORKED_EXAMPLES.add(new WorkedExample(
                "OS Page Size: 4 KiB Virtual Memory Page",
                "Virtual memory management pages are standardly 4 KiB = 4,096 bytes (2^12 bytes, matching a 12-bit offset).",
                BigDecimal.valueOf(4), Unit.KIBIBYTE, Unit.BYTE));

        WORKED_EXAMPLES.add(new WorkedExample(
                "SSD Drive: 500 GB Drive in GiB",
                "A 500 GB solid-state drive contains 500,000,000,000 bytes, which equals 465.66 GiB in binary measurement.",
                BigDecimal.valueOf(500), Unit.GIGABYTE, Unit.GIBIBYTE));
    }

    private MemoryUnitConverter() {
    }

    public static List<WorkedExample> getWorkedExamples() {
        return Collections.unmodifiableList(WORKED_EXAMPLES);
    }

    public static ConversionResult convert(BigDecimal input, Unit source, Unit target) {
        if (input == null) {
            throw new IllegalArgumentException("Input value cannot be null");
        }
        if (input.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Data size cannot be negative");
        }
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target units must be specified");
        }

        BigDecimal totalBits = input.multiply(source.getBitsMultiplier());
        BigDecimal totalBytes = totalBits.divide(BigDecimal.valueOf(8), 10, RoundingMode.HALF_UP);

        BigDecimal targetVal = totalBits.divide(target.getBitsMultiplier(), 12, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        if (targetVal.scale() < 0) {
            targetVal = targetVal.setScale(0, RoundingMode.UNNECESSARY);
        }

        List<String> stepList = new ArrayList<>();
        stepList.add(String.format(Locale.ROOT, "Input: %s %s (%s)", formatNumber(input), source.getSymbol(), source.getName()));
        stepList.add(String.format(Locale.ROOT, "Step 1: Convert to base bits: %s × %s bits = %s bits",
                formatNumber(input), source.getBitsMultiplier().toPlainString(), formatNumber(totalBits)));
        stepList.add(String.format(Locale.ROOT, "Step 2: Equivalent in bytes: %s bits ÷ 8 = %s bytes",
                formatNumber(totalBits), formatNumber(totalBytes)));
        stepList.add(String.format(Locale.ROOT, "Step 3: Convert to target unit %s: %s bits ÷ %s bits = %s %s",
                target.getSymbol(), formatNumber(totalBits), target.getBitsMultiplier().toPlainString(),
                formatNumber(targetVal), target.getSymbol()));

        String distinction = buildDistinctionExplanation(source, target);
        Map<Unit, String> equivalents = buildEquivalentsTable(totalBits);

        return new ConversionResult(
                input, source, target,
                targetVal, totalBits, totalBytes,
                formatNumber(targetVal) + " " + target.getSymbol(),
                stepList, distinction, equivalents
        );
    }

    public static String buildDistinctionExplanation(Unit source, Unit target) {
        StringBuilder sb = new StringBuilder();
        sb.append("DECIMAL (SI) vs BINARY (IEC) DISTINCTION:\n");
        sb.append("• SI Units (kB, MB, GB, TB): Based on powers of 10 (10^3 = 1,000).\n");
        sb.append("  Defined by the International System of Units. Used by disk manufacturers, flash drives, and networking.\n");
        sb.append("• IEC Units (KiB, MiB, GiB, TiB): Based on powers of 2 (2^10 = 1,024).\n");
        sb.append("  Standardized by IEC in 1998 to eliminate ambiguity. Used for RAM sizing, CPU cache, and operating systems.\n\n");

        sb.append("Key Multipliers:\n");
        sb.append("  1 kB  = 1,000 Bytes (10^3)           |  1 KiB = 1,024 Bytes (2^10)         [+2.4%]\n");
        sb.append("  1 MB  = 1,000,000 Bytes (10^6)       |  1 MiB = 1,048,576 Bytes (2^20)     [+4.86%]\n");
        sb.append("  1 GB  = 1,000,000,000 Bytes (10^9)   |  1 GiB = 1,073,741,824 Bytes (2^30) [+7.37%]\n");
        sb.append("  1 TB  = 10^12 Bytes                  |  1 TiB = 2^40 Bytes                 [+9.95%]\n\n");

        sb.append("Why the gap widens with larger units:\n");
        sb.append("The discrepancy grows exponentially with larger prefixes: (1024 / 1000)^n.\n");
        sb.append("At the Terabyte level, 1 TB is only ~931.32 GiB (a ~6.87% difference).\n");
        sb.append("At the Petabyte level, 1 PB is only ~888.18 PiB (a ~11.18% difference).\n");
        sb.append("This is why computers report lower storage capacity than advertised on the box!");
        return sb.toString();
    }

    private static Map<Unit, String> buildEquivalentsTable(BigDecimal totalBits) {
        Map<Unit, String> map = new LinkedHashMap<>();
        Unit[] order = new Unit[]{
                Unit.BIT, Unit.BYTE,
                Unit.KILOBYTE, Unit.KIBIBYTE,
                Unit.MEGABYTE, Unit.MEBIBYTE,
                Unit.GIGABYTE, Unit.GIBIBYTE,
                Unit.TERABYTE, Unit.TEBIBYTE,
                Unit.PETABYTE, Unit.PEBIBYTE
        };

        for (Unit u : order) {
            BigDecimal val = totalBits.divide(u.getBitsMultiplier(), 6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            map.put(u, formatNumber(val) + " " + u.getSymbol());
        }
        return map;
    }

    public static String formatNumber(BigDecimal number) {
        if (number == null) return "0";
        if (number.scale() <= 0 && number.compareTo(new BigDecimal("1000000000000000")) < 0) {
            DecimalFormat df = new DecimalFormat("#,##0", new java.text.DecimalFormatSymbols(Locale.US));
            return df.format(number.toBigInteger());
        } else {
            DecimalFormat df = new DecimalFormat("#,##0.######", new java.text.DecimalFormatSymbols(Locale.US));
            return df.format(number);
        }
    }
}
