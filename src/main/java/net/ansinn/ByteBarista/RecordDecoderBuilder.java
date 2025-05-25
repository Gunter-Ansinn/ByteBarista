package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;

public final class RecordDecoderBuilder {
    public static final ClassDesc BUFFER_DESC = ClassDesc.of("java.nio.ByteBuffer");
    public static final ClassDesc HELPER_DESC = ClassDesc.of("net.ansinn.ByteBarista.NumericHelpers");
    public static final ClassDesc ILLEGAL_ARGUMENT = ClassDesc.of("java.lang.IllegalArgumentException");

    public static final MethodTypeDesc BUFFER_LONG_LOAD = MethodTypeDesc.ofDescriptor("(Ljava.nio.ByteBuffer;)J");
    public static final MethodTypeDesc BUFFER_INT_LOAD = MethodTypeDesc.ofDescriptor("(Ljava.nio.ByteBuffer;)I");

    public static final MethodTypeDesc INT_DESC = MethodTypeDesc.ofDescriptor("()I");
    public static final MethodTypeDesc SHORT_DESC = MethodTypeDesc.ofDescriptor("()S");
    public static final MethodTypeDesc BYTE_DESC = MethodTypeDesc.ofDescriptor("()B");

    private RecordDecoderBuilder() {}

    public static BundledInfo bundleInfo(Class<? extends Record> recordClazz) throws IllegalAccessException, NoSuchMethodException {
        var lookup = MethodHandles.lookup();
        var hidden = lookup.defineHiddenClass(emitClassBytes(recordClazz), true);
        var clazz = hidden.lookupClass();
        var methodType = MethodType.methodType(Object.class, ByteBuffer.class);

        var unsafeHandle = hidden.findStatic(clazz, "loadFromBuffer_Unsafe", methodType);
        var safeHandle = hidden.findStatic(clazz, "loadFromBuffer_Safe", methodType);

        return new BundledInfo(safeHandle, unsafeHandle, MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("net.ansinn.ByteBarista", clazz.getSimpleName() + "_Decoder"),
                "loadFromBuffer_Unsafe",
                MethodTypeDesc.ofDescriptor("(Ljava.nio.ByteBuffer;)L" + clazz.getName())
        ));
    }

    public static byte[] emitClassBytes(Class<? extends Record> clazz) {
        var desc = ClassDesc.of("net.ansinn.ByteBarista", clazz.getSimpleName() + "_Decoder");

        return ClassFile.of().build(
                desc,
                builder -> {
                    builder.withFlags(0);

                    var methodType = MethodTypeDesc.of(ConstantDescs.CD_Object, BUFFER_DESC);

                    builder.withMethod("loadFromBuffer_Unsafe", methodType, ClassFile.ACC_STATIC, methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> populateConstructorFill(codeBuilder, clazz));
                    });

                    builder.withMethod("loadFromBuffer_Safe", methodType, ClassFile.ACC_STATIC, methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> {
                            populateSafetyException(codeBuilder, clazz);
                            populateConstructorFill(codeBuilder, clazz);
                        });
                    });
                }
        );
    }

    public static void populateConstructorFill(CodeBuilder builder, Class<? extends Record> clazz) {
        var components = clazz.getRecordComponents();
        var methodDesc = buildSignature(components);

        var index = 1;
        for (var component : components) {
            builder.aload(0);
            writeParserMethod(builder, component);
            builder.istore(index++);
        }

        builder.new_(ClassDesc.of(clazz.getName())).dup();

        for (int i = 1; i < index; i++) {
            builder.iload(i);
        }

        builder
                .invokespecial(
                        ClassDesc.of(clazz.getName()),
                        ConstantDescs.INIT_NAME,
                        MethodTypeDesc.ofDescriptor(methodDesc)
                ).areturn();
    }

    public static void populateSafetyException(CodeBuilder builder, Class<? extends Record> record) {
        var continueLabel = builder.newLabel();
        var size = ClassUtils.getRecordSize(record);

        builder
                .aload(0)
                .invokevirtual(BUFFER_DESC, "remaining", INT_DESC)
                .ldc(size)
                .if_icmpge(continueLabel)

                .new_(ILLEGAL_ARGUMENT)
                .dup()
                .ldc("Not enough bytes in ByteBuffer to load record: " + record.getSimpleName())
                .invokespecial(
                        ILLEGAL_ARGUMENT,
                        ConstantDescs.INIT_NAME,
                        MethodTypeDesc.ofDescriptor("(Ljava.lang.String;)V")
                )
                .athrow()

                .labelBinding(continueLabel);
    }

    public static void writeParserMethod(CodeBuilder codeBuilder, RecordComponent component) {
        var componentType = component.getType();

        switch (componentType.getTypeName()) {
            case "long" -> {
                if (component.isAnnotationPresent(UnsignedByte.class))
                    codeBuilder.invokevirtual(HELPER_DESC, "getUnsignedByteAsLong", BUFFER_LONG_LOAD);
                else if (component.isAnnotationPresent(UnsignedShort.class))
                    codeBuilder.invokevirtual(HELPER_DESC, "getUnsignedShortAsLong", BUFFER_LONG_LOAD);
                else if (component.isAnnotationPresent(UnsignedInteger.class))
                    codeBuilder.invokevirtual(HELPER_DESC, "getUnsignedInt", BUFFER_LONG_LOAD);
                else
                    codeBuilder.invokevirtual(BUFFER_DESC, "getLong", MethodTypeDesc.ofDescriptor("()J"));
            }
            case "int" -> {
                if (component.isAnnotationPresent(UnsignedByte.class))
                    codeBuilder.invokevirtual(HELPER_DESC, "getUnsignedByteAsInt", BUFFER_INT_LOAD);
                else if (component.isAnnotationPresent(UnsignedShort.class))
                    codeBuilder.invokevirtual(HELPER_DESC, "getUnsignedShortAsInt", BUFFER_INT_LOAD);
                else if (component.isAnnotationPresent(UnsignedInteger.class))
                    throw new IllegalStateException("You can't load an unsigned integer as an integer.");
                else
                    codeBuilder
                            .invokevirtual(BUFFER_DESC, "getInt", INT_DESC);
            }
            case "short" -> {
                codeBuilder
                        .invokevirtual(BUFFER_DESC, "getShort", SHORT_DESC);
            }
            case "byte" -> {
                codeBuilder
                        .invokevirtual(BUFFER_DESC, "get", BYTE_DESC);
            }

            case "double" -> {
                codeBuilder
                        .invokevirtual(BUFFER_DESC, "getDouble", MethodTypeDesc.ofDescriptor("()D"));
            }
            case "float" -> {
                codeBuilder
                        .invokevirtual(BUFFER_DESC, "getFloat", MethodTypeDesc.ofDescriptor("()F"));
            }

            case "char" -> {
                codeBuilder
                        .invokevirtual(BUFFER_DESC, "getChar", MethodTypeDesc.ofDescriptor("()C"));
            }

            default -> throw new IllegalStateException("Unexpected type: " + componentType.getTypeName());
        };
    }

    /**
     * Builds a method signature out of the parameter types within a record.
     * @param components
     * @return a string representing the signature of the method.
     */
    public static String buildSignature(RecordComponent[] components) {
        var builder = new StringBuilder("(");
        for (RecordComponent component : components) {
            builder.append(ClassUtils.getDescriptor(component));
        }
        return builder.append(")V").toString();
    }

    public record BundledInfo(MethodHandle safe, MethodHandle unsafe, MethodHandleDesc description) { }
}