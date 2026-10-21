package com.example.cscalc.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BitwiseOpsTest {

    @Test
    public void bitwiseAndOperation() {
        // 8-bit: 0b1100 & 0b1010 = 0b1000 (8)
        BitwiseOps.Result res = BitwiseOps.calculate(
                BitwiseOps.Operation.AND, BitwiseOps.BitWidth.WIDTH_8, 0b1100L, 0b1010L);

        assertEquals(0b1000L, res.getResultValue());
        assertEquals("0x08", res.getHexResult());
        assertEquals(8L, res.getUnsignedResult());
        assertEquals(8L, res.getSignedResult());
        assertTrue(res.getBitGridComparison().contains("&"));
    }

    @Test
    public void bitwiseOrOperation() {
        BitwiseOps.Result res = BitwiseOps.calculate(
                BitwiseOps.Operation.OR, BitwiseOps.BitWidth.WIDTH_8, 0b1100L, 0b0011L);

        assertEquals(0b1111L, res.getResultValue());
        assertEquals("0x0F", res.getHexResult());
    }

    @Test
    public void bitwiseXorOperation() {
        BitwiseOps.Result res = BitwiseOps.calculate(
                BitwiseOps.Operation.XOR, BitwiseOps.BitWidth.WIDTH_8, 0b11110000L, 0b10101010L);

        assertEquals(0b01011010L, res.getResultValue());
        assertEquals("0x5A", res.getHexResult());
    }

    @Test
    public void boundedNotResultPreventsUnboundedExtension() {
        // 8-bit NOT of 0x01 should be 0xFE (254), NOT unbounded 64-bit 0xFFFFFFFFFFFFFFFEL
        BitwiseOps.Result res8 = BitwiseOps.calculate(
                BitwiseOps.Operation.NOT, BitwiseOps.BitWidth.WIDTH_8, 0x01L, null);
        assertEquals(0xFEL, res8.getResultValue());
        assertEquals("0xFE", res8.getHexResult());
        assertEquals(254L, res8.getUnsignedResult());
        assertEquals(-2L, res8.getSignedResult());

        // 16-bit NOT of 0x0000 should be 0xFFFF (65535)
        BitwiseOps.Result res16 = BitwiseOps.calculate(
                BitwiseOps.Operation.NOT, BitwiseOps.BitWidth.WIDTH_16, 0x0000L, null);
        assertEquals(0xFFFFL, res16.getResultValue());
        assertEquals("0xFFFF", res16.getHexResult());

        // 32-bit NOT of 0x00000000 should be 0xFFFFFFFF
        BitwiseOps.Result res32 = BitwiseOps.calculate(
                BitwiseOps.Operation.NOT, BitwiseOps.BitWidth.WIDTH_32, 0L, null);
        assertEquals(0xFFFFFFFFL, res32.getResultValue());
        assertEquals("0xFFFFFFFF", res32.getHexResult());
    }

    @Test
    public void leftShiftWithFixedMasking() {
        // 8-bit: 0x81 (1000 0001) << 1 = 0x02 (0000 0010), bit 7 pushed off is masked out
        BitwiseOps.Result res = BitwiseOps.calculate(
                BitwiseOps.Operation.LEFT_SHIFT, BitwiseOps.BitWidth.WIDTH_8, 0x81L, 1L);

        assertEquals(0x02L, res.getResultValue());
        assertEquals("0x02", res.getHexResult());
    }

    @Test
    public void logicalRightShiftZeroFills() {
        // 8-bit: 0x80 (1000 0000) >>> 1 = 0x40 (0100 0000)
        BitwiseOps.Result res = BitwiseOps.calculate(
                BitwiseOps.Operation.RIGHT_SHIFT_LOGICAL, BitwiseOps.BitWidth.WIDTH_8, 0x80L, 1L);

        assertEquals(0x40L, res.getResultValue());
        assertEquals("0x40", res.getHexResult());
        assertEquals(64L, res.getUnsignedResult());
    }

    @Test
    public void arithmeticRightShiftSignExtends() {
        // 8-bit: 0x80 (signed -128, MSB=1) >> 1 = 0xC0 (1100 0000, signed -64)
        BitwiseOps.Result res = BitwiseOps.calculate(
                BitwiseOps.Operation.RIGHT_SHIFT_ARITHMETIC, BitwiseOps.BitWidth.WIDTH_8, 0x80L, 1L);

        assertEquals(0xC0L, res.getResultValue());
        assertEquals("0xC0", res.getHexResult());
        assertEquals(192L, res.getUnsignedResult());
        assertEquals(-64L, res.getSignedResult());
    }

    @Test
    public void signednessInterpretation() {
        // 8-bit: 0xFF is unsigned 255, signed -1
        assertEquals(-1L, BitwiseOps.toSigned(0xFFL, BitwiseOps.BitWidth.WIDTH_8));
        // 16-bit: 0x8000 is unsigned 32768, signed -32768
        assertEquals(-32768L, BitwiseOps.toSigned(0x8000L, BitwiseOps.BitWidth.WIDTH_16));
        // 8-bit: 0x7F is unsigned 127, signed 127
        assertEquals(127L, BitwiseOps.toSigned(0x7FL, BitwiseOps.BitWidth.WIDTH_8));
    }

    @Test
    public void inputParsingFormats() {
        assertEquals(15L, BitwiseOps.parseInput("0x0F", BitwiseOps.BitWidth.WIDTH_8, 10));
        assertEquals(5L, BitwiseOps.parseInput("0b101", BitwiseOps.BitWidth.WIDTH_8, 10));
        assertEquals(42L, BitwiseOps.parseInput("42", BitwiseOps.BitWidth.WIDTH_8, 10));
    }
}
