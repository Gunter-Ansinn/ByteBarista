package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SafeRecordDecoderTest {

    @Test
    void testDecodeRecord() {

        // Test Normal values within records
        var targetValueNormal = new decoderTest(10, 1.0f, 'a');

    }

    /**
     * Test record decoding for unsigned numbers as annotated by the @Unsigned[Type] annotations
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    @Test
    void testUnsignedRecordDecode() throws IllegalAccessException, NoSuchMethodException {
        var bufferSize = ClassUtils.getRecordSize(UnsignedTest.class);
        var target = new UnsignedTest(0xFF, 0xFFFF, 0xFFFFFFFFL);

        var buffer = ByteBuffer.allocate(bufferSize);

        buffer.put((byte) 0xFF);           // Unsigned byte max
        buffer.putShort((short) 0xFFFF);   // Unsigned short max
        buffer.putInt((int) 0xFFFFFFFFL);

        buffer.flip();

        var output = SafeRecordDecoder.decodeRecord(buffer, UnsignedTest.class);

        assertNotNull(output, "output should not be null");

        assertEquals(target.uByte, output.uByte, "Mismatch between unsigned byte target and unsigned byte output");
        assertEquals(target.uShort, output.uShort, "Mismatch between unsigned short target and unsigned short output");
        assertEquals(target.uInt, output.uInt, "Mismatch between unsigned int target and unsigned int output");
    }

    void testInternalRecordDecode() {
        // Test inline record classes.
        record test(int a, float b) {}
        var targetValueInternal = new test(5000000, 0.25f);
    }

    @Test
    void getDeserializer() {

    }

    public record decoderTest(int numberInt, float numberFloat, char character) { }
    public record UnsignedTest(@UnsignedByte int uByte, @UnsignedShort int uShort, @UnsignedInteger long uInt) {}

}