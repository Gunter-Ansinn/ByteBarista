package net.ansinn.ByteBarista.codegen.stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;
import net.ansinn.ByteBarista.codegen.CodegenConstants;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.classfile.ClassFile;

public class TestBuildDecoder {

    @Test
    void testBuildByteRecord() {
        record ByteRecord(byte value) {}
        assertDoesNotThrow(() -> buildDecoderClass(ByteRecord.class));
    }

    @Test
    void testBuildShortRecord() {
        record ShortRecord(short value) {}
        assertDoesNotThrow(() -> buildDecoderClass(ShortRecord.class));
    }

    @Test
    void testBuildIntRecord() {
        record IntRecord(int value) {}
        assertDoesNotThrow(() -> buildDecoderClass(IntRecord.class));
    }

    @Test
    void testBuildLongRecord() {
        record LongRecord(long value) {}
        assertDoesNotThrow(() -> buildDecoderClass(LongRecord.class));
    }

    @Test
    void testBuildFloatRecord() {
        record FloatRecord(float value) {}
        assertDoesNotThrow(() -> buildDecoderClass(FloatRecord.class));
    }

    @Test
    void testBuildDoubleRecord() {
        record DoubleRecord(double value) {}
        assertDoesNotThrow(() -> buildDecoderClass(DoubleRecord.class));
    }

    @Test
    void testBuildCharRecord() {
        record CharRecord(char value) {}
        assertDoesNotThrow(() -> buildDecoderClass(CharRecord.class));
    }


    @Test
    void testBuildUnsignedByteRecord() {
        record UnsignedByteRecord(@UnsignedByte int value) {}
        assertDoesNotThrow(() -> buildDecoderClass(UnsignedByteRecord.class));
    }

    @Test
    void testBuildUnsignedShortRecord() {
        record UnsignedShortRecord(@UnsignedShort int value) {}
        assertDoesNotThrow(() -> buildDecoderClass(UnsignedShortRecord.class));
    }

    @Test
    void testBuildUnsignedIntRecord() {
        record UnsignedIntRecord(@UnsignedInteger long value) {}
        assertDoesNotThrow(() -> buildDecoderClass(UnsignedIntRecord.class));
    }

    private void buildDecoderClass(Class<? extends Record> clazz) {
        ClassDesc desc = ClassDesc.of("net.ansinn.ByteBarista.codegen.stream.GeneratedDecoder_" + clazz.getSimpleName());
        ClassFile.of().build(
            desc,
            builder -> {
                builder.withFlags(0);
                var methodType = MethodTypeDesc.of(clazz.describeConstable().orElseThrow(), CodegenConstants.INPUT_DESC);
                builder.withMethod("decode", methodType, ClassFile.ACC_STATIC, methodBuilder -> {
                    methodBuilder.withCode(codeBuilder -> StreamDecoderBuilder.emitReadFunction(codeBuilder, clazz));
                });
            }
        );
    }
}
