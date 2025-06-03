package net.ansinn.ByteBarista.codegen.stream;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

import static net.ansinn.ByteBarista.codegen.CodegenConstants.*;
import static net.ansinn.ByteBarista.codegen.DecoderUtils.*;
import static net.ansinn.ByteBarista.codegen.buffer.BufferDecoderBuilder.buildSignature;

/**
 * Builds a dynamically generated decoder for deserializing {@link Record} instances
 * from a {@link java.io.ByteArrayInputStream}. Each field is loaded in order based
 * on its primitive type and optional unsigned annotations.
 *
 * <p>Supports {@code byte}, {@code short}, {@code int}, {@code long}, {@code float},
 * {@code double}, and {@code char} primitives with optional annotations such as
 * {@link net.ansinn.ByteBarista.annotations.UnsignedByte} to reinterpret input data
 * in an unsigned form.
 *
 * @author Gunter Ansinn
 */
public final class StreamDecoderBuilder {

    public StreamDecoderBuilder() {}

    /**
     * Emits bytecode to decode a full {@link Record} from a {@link java.io.ByteArrayInputStream}.
     * This function generates code that:
     * <ul>
     *     <li>Parses each record field using its associated primitive type.</li>
     *     <li>Handles unsigned annotations where applicable.</li>
     *     <li>Stores fields into local slots with correct typed instructions.</li>
     *     <li>Constructs and returns a new record instance using the parsed values.</li>
     * </ul>
     *
     * @param builder the {@link CodeBuilder} used to emit bytecode for the decoding method
     * @param clazz the record class to generate a decoder for
     */
    static void emitReadFunction(CodeBuilder builder, Class<? extends Record> clazz) {
        // Build signature for record parameters to be used with constructor invocation
        var components = clazz.getRecordComponents();
        var methodDesc = buildSignature(components);

        var index = 1;

        // Iterate over every single record parameter emitting read functions for the bytebuffer
        // located in address 0 (since this method is static and not local)
        for (var component : components) {
            var type = component.getType();
            // Write instructions to load primitives
            if (type.isPrimitive())
                writePrimitiveParser(builder, component);
            // Write instructions to load records and arrays of records
            else
                writeClassParser(builder, component);

            storeType(builder, component, index);
            index += slotsFor(component);
        }

        // Create allocation instruction
        builder.new_(ClassDesc.of(clazz.getName())).dup();

        // Load in every variable for insertion into the constructor
        var cursor = 1;
        for (var component : components) {
            loadType(builder, component, cursor);
            cursor += slotsFor(component);
        }

        // Emit instruction to return new value of class
        builder
                .invokespecial(
                        ClassDesc.of(clazz.getName()),
                        ConstantDescs.INIT_NAME,
                        MethodTypeDesc.ofDescriptor(methodDesc)
        ).areturn();
    }

    /**
     * Emits bytecode to read a single primitive value from the stream.
     * Handles dispatch based on the field's declared type and any associated
     * unsigned annotations. Composite decoding logic is delegated to type-specific emitters.
     *
     * @param builder the {@link CodeBuilder} to emit bytecode into
     * @param component the record component being decoded
     */
    static void writePrimitiveParser(CodeBuilder builder, RecordComponent component) {
        var componentType = component.getType();

        switch (componentType.getName()) {
            case "long" ->
                    emitLongInfo(builder, component);
            case "int" ->
                    emitIntInfo(builder, component);
            case "short" ->
                    emitReadFromShortStream(builder);
            case "byte" ->
                    emitReadFromByteStream(builder);

            case "double" ->
                    emitReadFromDoubleStream(builder);
            case "float" ->
                    emitReadFromFloatStream(builder);

            case "char" ->
                    emitReadFromCharStream(builder);

            default -> throw new IllegalStateException("Unexpected type: " + componentType.getTypeName());
        }
    }

    /**
     * Emits logic to read a {@code long} from the stream. Supports optional
     * reinterpretation of smaller unsigned types into {@code long} using annotations:
     * <ul>
     *     <li>{@link UnsignedByte}: 1 byte → int → long</li>
     *     <li>{@link UnsignedShort}: 2 bytes → int → long</li>
     *     <li>{@link UnsignedInteger}: 4 bytes → long</li>
     * </ul>
     *
     * @param builder the {@link CodeBuilder} to emit bytecode into
     * @param component the record component being decoded
     */
    private static void emitLongInfo(CodeBuilder builder, RecordComponent component) {
        if (component.isAnnotationPresent(UnsignedByte.class))
            emitReadFromByteToLongStream(builder); // 1 byte -> int -> long
        else if (component.isAnnotationPresent(UnsignedShort.class))
            emitReadFromShortToLongStream(builder); // 2 bytes -> int -> long
        else if (component.isAnnotationPresent(UnsignedInteger.class))
            emitReadFromIntUnsignedStream(builder); // 4 bytes -> long
        else
            emitReadFromLongStream(builder); // full 8-byte long
    }

    private static void emitReadFromLongStream(CodeBuilder builder) {
        for (int shift : new int[] {56, 48, 40, 32, 24, 16, 8, 0}) {
            builder
                    .aload(0)
                    .invokevirtual(INPUT_DESC, "read", INT_DESC)
                    .sipush(0xFF)
                    .iand();
            if (shift > 0) builder.bipush(shift).i2l().lshl();
            else builder.i2l();
            if (shift < 56) builder.lor();
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
     * @param builder the {@link CodeBuilder} to emit bytecode into
     * @param component the record component being decoded
     * @throws IllegalStateException if {@link UnsignedInteger} is incorrectly applied to an {@code int}
     */
    private static void emitIntInfo(CodeBuilder builder, RecordComponent component) {
        if (component.isAnnotationPresent(UnsignedByte.class))
            emitReadFromByteToIntStream(builder);
        else if (component.isAnnotationPresent(UnsignedShort.class))
            emitReadFromShortToIntStream(builder);
        else if (component.isAnnotationPresent(UnsignedInteger.class))
            throw new IllegalStateException("You can't load an unsigned integer as an integer.");
        else
            emitReadFromIntStream(builder);
    }

    private static void emitReadFromIntStream(CodeBuilder builder) {
        for (int shift : new int[] {24, 16, 8, 0}) {
            builder
                    .aload(0)
                    .invokevirtual(INPUT_DESC, "read", INT_DESC);
            if (shift > 0) builder.bipush(shift).ishl();
            if (shift < 24) builder.ior();
        }
    }

    private static void emitReadFromIntUnsignedStream(CodeBuilder builder) {
        for (int shift : new int[] {24, 16, 8, 0}) {
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

    private static void emitReadFromShortStream(CodeBuilder builder) {
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

    private static void emitReadFromShortToLongStream(CodeBuilder builder) {
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

    private static void emitReadFromShortToIntStream(CodeBuilder builder) {
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

    private static void emitReadFromByteStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .i2b();
    }

    private static void emitReadFromByteToLongStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .bipush(0xFF)
                .iand()
                .i2l();
    }

    private static void emitReadFromByteToIntStream(CodeBuilder builder) {
        builder
                .aload(0)
                .invokevirtual(INPUT_DESC, "read", INT_DESC)
                .sipush(0xFF)
                .iand();
    }

    private static void emitReadFromDoubleStream(CodeBuilder builder) {
        emitReadFromLongStream(builder);
        builder.invokestatic(DOUBLE_DESC, "longBitsToDouble", MethodTypeDesc.ofDescriptor("(J)D"));
    }

    private static void emitReadFromFloatStream(CodeBuilder builder) {
        emitReadFromIntStream(builder);
        builder.invokestatic(FLOAT_DESC, "intBitsToFloat", MethodTypeDesc.ofDescriptor("(I)F"));
    }

    private static void emitReadFromCharStream(CodeBuilder builder) {
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

    /**
     * Placeholder method for decoding nested record types from the stream.
     * Currently unimplemented. Will throw {@link UnsupportedOperationException}.
     *
     * @param builder the {@link CodeBuilder} to emit bytecode into
     * @param component the nested record component
     * @throws UnsupportedOperationException always
     */
    private static void writeClassParser(CodeBuilder builder, RecordComponent component) {
        throw new UnsupportedOperationException("Todo, not yet implemented.");
    }
}
