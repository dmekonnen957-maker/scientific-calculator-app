package com.example.cscalc.calculator;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MemoryUnitConverterTest {

    @Test
    public void binaryVsDecimalKiloUnits() {
        // 1 kB = 1000 Bytes
        MemoryUnitConverter.ConversionResult resDecimal = MemoryUnitConverter.convert(
                BigDecimal.ONE, MemoryUnitConverter.Unit.KILOBYTE, MemoryUnitConverter.Unit.BYTE);
        assertEquals(new BigDecimal("1000"), resDecimal.getTargetValue());

        // 1 KiB = 1024 Bytes
        MemoryUnitConverter.ConversionResult resBinary = MemoryUnitConverter.convert(
                BigDecimal.ONE, MemoryUnitConverter.Unit.KIBIBYTE, MemoryUnitConverter.Unit.BYTE);
        assertEquals(new BigDecimal("1024"), resBinary.getTargetValue());
    }

    @Test
    public void terabyteToGibibyteDiscrepancy() {
        // 1 TB = 1,000,000,000,000 Bytes -> ~931.322574615 GiB
        MemoryUnitConverter.ConversionResult res = MemoryUnitConverter.convert(
                BigDecimal.ONE, MemoryUnitConverter.Unit.TERABYTE, MemoryUnitConverter.Unit.GIBIBYTE);

        assertTrue(res.getFormattedTarget().contains("931.32"));
        assertTrue(res.getDistinctionExplanation().contains("931.32"));
    }

    @Test
    public void ramModuleToBytesAndBits() {
        // 16 GiB = 17,179,869,184 Bytes
        MemoryUnitConverter.ConversionResult res = MemoryUnitConverter.convert(
                BigDecimal.valueOf(16), MemoryUnitConverter.Unit.GIBIBYTE, MemoryUnitConverter.Unit.BYTE);

        assertEquals(new BigDecimal("17179869184"), res.getTargetValue());
        assertEquals(new BigDecimal("137438953472"), res.getExactBits());
    }

    @Test
    public void workedExamplesAreAvailable() {
        List<MemoryUnitConverter.WorkedExample> examples = MemoryUnitConverter.getWorkedExamples();
        assertTrue(examples.size() >= 4);
        assertNotNull(examples.get(0).getTitle());
        assertNotNull(examples.get(0).getDescription());
    }

    @Test
    public void negativeInputThrowsException() {
        try {
            MemoryUnitConverter.convert(
                    BigDecimal.valueOf(-5), MemoryUnitConverter.Unit.BYTE, MemoryUnitConverter.Unit.KILOBYTE);
            fail("Expected IllegalArgumentException for negative size");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("negative"));
        }
    }

    @Test
    public void equivalentsTableContainsAllUnits() {
        MemoryUnitConverter.ConversionResult res = MemoryUnitConverter.convert(
                BigDecimal.valueOf(1024), MemoryUnitConverter.Unit.MEGABYTE, MemoryUnitConverter.Unit.GIGABYTE);
        assertTrue(res.getEquivalentValues().size() >= 10);
        assertTrue(res.getEquivalentValues().containsKey(MemoryUnitConverter.Unit.GIGABYTE));
        assertTrue(res.getEquivalentValues().containsKey(MemoryUnitConverter.Unit.GIBIBYTE));
    }
}
