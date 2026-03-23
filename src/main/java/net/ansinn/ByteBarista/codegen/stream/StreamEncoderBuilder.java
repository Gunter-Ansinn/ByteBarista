package net.ansinn.ByteBarista.codegen.stream;

import net.ansinn.ByteBarista.ClassUtils;
import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;
import net.ansinn.ByteBarista.codegen.CodegenConstants;

import java.io.OutputStream;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

import static net.ansinn.ByteBarista.ClassUtils.getField;
import static net.ansinn.ByteBarista.codegen.CodegenConstants.*;
import static net.ansinn.ByteBarista.codegen.CodegenConstants.WRITE_BYTE;

public final class StreamEncoderBuilder {

    private StreamEncoderBuilder() {}

    static void emitWriteFunction(CodeBuilder builder, Class<? extends Record> clazz) {
        var components = clazz.getRecordComponents();

        for (var component : components) {
            var type = component.getType();

            if (type.isPrimitive())
                writePrimitiveEncoder(builder, component);
            else
                writeClassEncoder(builder, component);

        }

        builder.return_();
    }

    private static void writePrimitiveEncoder(CodeBuilder builder, RecordComponent component) {
        var parentClazz = component.getDeclaringRecord();
        var componentType = component.getType();

        // Load Record from component
        builder.aload(1);
        builder.invokevirtual(ClassDesc.of(parentClazz.getName()), component.getName(), getField(component));

        // Load the stream
        builder.aload(0);
        builder.swap();

        switch (componentType.getName()) {
            case "long" ->
                emitWriteLongInfo(builder, component);
            case "int" ->
                emitWriteIntInfo(builder, component);
            case "short" ->
                emitWriteToShortStream(builder, component);
            case "byte" ->
                    builder.invokevirtual(INPUT_DESC, "write", WRITE_BYTE);

            case "double" ->
                emitWriteToDoubleStream(builder, component);
            case "float" ->
                emitWriteToFloatStream(builder, component);

            case "char" ->
                emitWriteToCharStream(builder, component);

            default -> throw new IllegalStateException("Unexpected type: " + componentType.getTypeName());
        }
    }

    private static void emitWriteLongInfo(CodeBuilder builder, RecordComponent component) {
        if (component.isAnnotationPresent(UnsignedByte.class))
            emitWriteToByteFromLongStream(builder, component);
        else if (component.isAnnotationPresent(UnsignedShort.class))
            emitWriteToShortFromLongStream(builder, component);
        else if (component.isAnnotationPresent(UnsignedInteger.class))
            emitWriteToIntFromLongStream(builder, component);
        else
            emitWriteToLongStream(builder, component);
    }

    private static void emitWriteToLongStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteIntInfo(CodeBuilder builder, RecordComponent component) {
        if (component.isAnnotationPresent(UnsignedByte.class))
            emitWriteToByteFromIntStream(builder, component);
        else if (component.isAnnotationPresent(UnsignedShort.class))
            emitWriteToShortFromIntStream(builder, component);
        else if (component.isAnnotationPresent(UnsignedInteger.class))
            throw new IllegalStateException("You can't load an unsigned integer as an integer.");
        else
            emitWriteToIntStream(builder, component);
    }

    private static void emitWriteToIntFromLongStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToIntStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToShortStream(CodeBuilder builder, RecordComponent component) {
        // >> 8
        builder.dup();
        builder.bipush(8);
        builder.ishr();
        builder.aload(0);
        builder.swap();
        builder.invokevirtual(INPUT_DESC, "write", WRITE_BYTE);

        // & 0xFF
        builder.swap();
        builder.bipush(0xFF);
        builder.iand();
        builder.aload(0);
        builder.swap();
        builder.invokevirtual(INPUT_DESC, "write", WRITE_BYTE);
    }

    private static void emitWriteToShortFromLongStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToShortFromIntStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToByteStream(CodeBuilder builder, RecordComponent component) {
    }

    private static void emitWriteToByteFromLongStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToByteFromIntStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToDoubleStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToFloatStream(CodeBuilder builder, RecordComponent component) {

    }

    private static void emitWriteToCharStream(CodeBuilder builder, RecordComponent component) {
        // >> 8
        builder.dup();
        builder.bipush(8);
        builder.iushr();
        builder.aload(0);
        builder.swap();
        builder.invokevirtual(INPUT_DESC, "write", WRITE_BYTE);

        // & 0xFF
        builder.swap();
        builder.bipush(0xFF);
        builder.iand();
        builder.aload(0);
        builder.swap();
        builder.invokevirtual(INPUT_DESC, "write", WRITE_BYTE);
    }

    private static void writeClassEncoder(CodeBuilder builder, RecordComponent component) {
        throw new UnsupportedOperationException("Non-primitive type encoding not yet supported: " + component.getType());
    }

}
