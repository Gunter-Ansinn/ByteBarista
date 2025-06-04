package net.ansinn.ByteBarista.codegen.stream;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;
import net.ansinn.ByteBarista.codegen.CodegenConstants;

import java.io.ByteArrayInputStream;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//TODO swap manual inputs wherever possible to encoder implementation.
public class TestDecoderMethod {

    private static MethodHandle getTestHandle(Class<? extends Record> recordClazz) throws IllegalAccessException, NoSuchMethodException {
        var lookup = MethodHandles.lookup();
        var hidden = lookup.defineHiddenClass(emitStreamDecoderBytes(recordClazz), true);
        var clazz = hidden.lookupClass();
        var methodType = MethodType.methodType(Object.class, ByteArrayInputStream.class);

        return hidden.findStatic(clazz, "decode", methodType);
    }

    private static byte[] emitStreamDecoderBytes(Class<? extends Record> clazz) {
        var desc = ClassDesc.of("net.ansinn.ByteBarista.codegen.stream", clazz.getSimpleName() + "_StreamCodec");

        return ClassFile.of().build(
                desc,
                builder -> {
                    builder.withFlags(0);

                    var methodType = MethodTypeDesc.of(ConstantDescs.CD_Object, CodegenConstants.INPUT_DESC);

                    builder.withMethod("decode", methodType, ClassFile.ACC_STATIC, methodBuilder ->
                            methodBuilder.withCode(codeBuilder ->
                                    StreamDecoderBuilder.emitReadFunction(codeBuilder, clazz)
                            )
                    );
                }
        );
    }

    @Test
    void testDecodeByte() throws Throwable {
        record ByteRecord(byte value) {}
        var handle = getTestHandle(ByteRecord.class);
        var input = new ByteArrayInputStream(new byte[]{(byte) 0x7F});
        var result = (ByteRecord) handle.invoke(input);
        assertEquals((byte) 127, result.value());
    }

    @Test
    void testDecodeShort() throws Throwable {
        record ShortRecord(short value) {}
        var handle = getTestHandle(ShortRecord.class);
        var input = new ByteArrayInputStream(new byte[]{0x00, 0x2A});
        var result = (ShortRecord) handle.invoke(input);
        assertEquals((short) 42, result.value());
    }

    @Test
    void testDecodeInt() throws Throwable {
        record IntRecord(int value) {}
        var handle = getTestHandle(IntRecord.class);
        var input = new ByteArrayInputStream(new byte[]{0x00, 0x00, 0x00, 0x2A});
        var result = (IntRecord) handle.invoke(input);
        assertEquals(42, result.value());
    }

    @Test
    void testDecodeLong() throws Throwable {
        record LongRecord(long value) {}
        var handle = getTestHandle(LongRecord.class);
        var input = new ByteArrayInputStream(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2A});
        var result = (LongRecord) handle.invoke(input);
        assertEquals(42L, result.value());
    }

    @Test
    void testDecodeFloat() throws Throwable {
        record FloatRecord(float value) {}
        var handle = getTestHandle(FloatRecord.class);
        var input = new ByteArrayInputStream(new byte[]{0x42, 0x28, 0x00, 0x00}); // 42.0f
        var result = (FloatRecord) handle.invoke(input);
        assertEquals(42.0f, result.value(), 0.0001);
    }

    @Test
    void testDecodeDouble() throws Throwable {
        record DoubleRecord(double value) {}
        var handle = getTestHandle(DoubleRecord.class);
        var input = new ByteArrayInputStream(new byte[]{0x40, 0x45, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}); // 42.0
        var result = (DoubleRecord) handle.invoke(input);
        assertEquals(42.0, result.value(), 0.0001);
    }

    @Test
    void testDecodeChar() throws Throwable {
        record CharRecord(char value) {}
        var handle = getTestHandle(CharRecord.class);
        var input = new ByteArrayInputStream(new byte[]{0x00, 0x41});
        var result = (CharRecord) handle.invoke(input);
        assertEquals('A', result.value());
    }

    @Test
    void testDecodeUnsignedByte() throws Throwable {
        record UnsignedByteRecord(@UnsignedByte int value) {}
        var handle = getTestHandle(UnsignedByteRecord.class);
        var input = new ByteArrayInputStream(new byte[]{(byte) 0xFF});
        var result = (UnsignedByteRecord) handle.invoke(input);
        assertEquals(255, result.value());
    }

    @Test
    void testDecodeUnsignedShort() throws Throwable {
        record UnsignedShortRecord(@UnsignedShort int value) {}
        var handle = getTestHandle(UnsignedShortRecord.class);
        var input = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xFF});
        var result = (UnsignedShortRecord) handle.invoke(input);
        assertEquals(65535, result.value());
    }

    @Test
    void testDecodeUnsignedInt() throws Throwable {
        record UnsignedIntRecord(@UnsignedInteger long value) {}
        var handle = getTestHandle(UnsignedIntRecord.class);
        var input = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        var result = (UnsignedIntRecord) handle.invoke(input);
        assertEquals(0xFFFFFFFFL, result.value());
    }
}
