package net.ansinn.ByteBarista.codegen.stream;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

import static net.ansinn.ByteBarista.codegen.CodegenConstants.*;

public class PrimitiveStreamWriters {

    /**
     * Emits logic to read a {@code long} from the stream. Supports optional
     * reinterpretation of smaller unsigned types into {@code long} using annotations:
     * <ul>
     *     <li>{@link UnsignedByte}: 1 byte → int → long</li>
     *     <li>{@link UnsignedShort}: 2 bytes → int → long</li>
     *     <li>{@link UnsignedInteger}: 4 bytes → long</li>
     * </ul>
     *
     * @param builder   the {@link CodeBuilder} to emit bytecode into
     * @param component the record component being decoded
     */
    static void emitReadLongInfo(CodeBuilder builder, RecordComponent component) {
        if (component.isAnnotationPresent(UnsignedByte.class))
            emitReadFromByteToLongStream(builder); // 1 byte -> int -> long
        else if (component.isAnnotationPresent(UnsignedShort.class))
            emitReadFromShortToLongStream(builder); // 2 bytes -> int -> long
        else if (component.isAnnotationPresent(UnsignedInteger.class))
            emitReadFromIntUnsignedStream(builder); // 4 bytes -> long
        else
            emitReadFromLongStream(builder); // full 8-byte long
    }

    static void emitReadFromLongStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .sipush(0xFF)
                .iand()
                .i2l()
                .bipush(56)
                .lshl(); // initialize value on the stack with MSB

        for (int i = 1; i < 8; i++) {
            int shift = 8 * (7 - i);

            builder
                    .aload(0)
                    .invokevirtual(INPUT_DESC, "read", INT_DESC)
                    .sipush(0xFF)
                    .iand()
                    .i2l();

            if (shift > 0)
                builder.bipush(shift).lshl();

            builder.lor();
        }
    }

    /**
     * Emits logic to read an {@code int} from the stream. Supports optional
     * reinterpretation of smaller unsigned types into {@code int} using annotations:
     * <ul>
     *     <li>{@link UnsignedByte}: 1 byte → int</li>
     *     <li>{@link UnsignedShort}: 2 bytes → int</li>
     * </ul>
     * <p>{@link UnsignedInteger} is not valid on int and will throw.</p>
     *
     * @param builder   the {@link CodeBuilder} to emit bytecode into
     * @param component the record component being decoded
     * @throws IllegalStateException if {@link UnsignedInteger} is incorrectly applied to an {@code int}
     */
    static void emitReadIntInfo(CodeBuilder builder, RecordComponent component) {
        if (component.isAnnotationPresent(UnsignedByte.class))
            emitReadFromByteToIntStream(builder);
        else if (component.isAnnotationPresent(UnsignedShort.class))
            emitReadFromShortToIntStream(builder);
        else if (component.isAnnotationPresent(UnsignedInteger.class))
            throw new IllegalStateException("You can't load an unsigned integer as an integer.");
        else
            emitReadFromIntStream(builder);
    }

    static void emitReadFromIntStream(CodeBuilder builder) {
        for (int shift : new int[]{24, 16, 8, 0}) {
            builder
                    .aload(0)
                    .invokevirtual(INPUT_DESC, "read", INT_DESC);
            if (shift > 0) builder.bipush(shift).ishl();
            if (shift < 24) builder.ior();
        }
    }

    static void emitReadFromIntUnsignedStream(CodeBuilder builder) {
        for (int shift : new int[]{24, 16, 8, 0}) {
            builder
                    .aload(0)
                    .invokevirtual(INPUT_DESC, "read", INT_DESC)
                    .sipush(0xFF)
                    .iand()
                    .i2l();
            if (shift > 0) builder.bipush(shift).lshl();
            if (shift < 24) builder.lor();
        }
    }

    static void emitReadFromShortStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .bipush(8)
                .ishl();
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .ior();
    }

    static void emitReadFromShortToLongStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .bipush(0xFF)
                .iand()
                .i2l()
                .bipush(8)
                .lshl();
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .bipush(0xFF)
                .iand()
                .i2l()
                .lor();
    }

    static void emitReadFromShortToIntStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .sipush(0xFF)
                .iand()
                .bipush(8)
                .ishl();
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .sipush(0xFF)
                .iand()
                .ior();
    }

    static void emitReadFromByteStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .i2b();
    }

    static void emitReadFromByteToLongStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .bipush(0xFF)
                .iand()
                .i2l();
    }

    static void emitReadFromByteToIntStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .sipush(0xFF)
                .iand();
    }

    static void emitReadFromDoubleStream(CodeBuilder builder) {
        emitReadFromLongStream(builder);
        builder.invokestatic(DOUBLE_DESC, "longBitsToDouble", MethodTypeDesc.ofDescriptor("(J)D"));
    }

    static void emitReadFromFloatStream(CodeBuilder builder) {
        emitReadFromIntStream(builder);
        builder.invokestatic(FLOAT_DESC, "intBitsToFloat", MethodTypeDesc.ofDescriptor("(I)F"));
    }

    static void emitReadFromCharStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .bipush(8)
                .ishl();
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .ior()
                .i2c();
    }
}
