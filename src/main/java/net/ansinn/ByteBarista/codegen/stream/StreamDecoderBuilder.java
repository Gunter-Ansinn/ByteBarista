package net.ansinn.ByteBarista.codegen.stream;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

import static net.ansinn.ByteBarista.codegen.DecoderUtils.*;
import static net.ansinn.ByteBarista.codegen.buffer.BufferDecoderBuilder.buildSignature;
import static net.ansinn.ByteBarista.codegen.stream.PrimitiveStreamWriters.*;

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
                    emitReadLongInfo(builder, component);
            case "int" ->
                    emitReadIntInfo(builder, component);
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
