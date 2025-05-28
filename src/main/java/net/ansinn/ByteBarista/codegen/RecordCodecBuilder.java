package net.ansinn.ByteBarista.codegen;

import java.lang.classfile.ClassFile;
import java.lang.constant.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

public final class RecordCodecBuilder {

    private RecordCodecBuilder() {}

    public static BundledInfo bundleInfo(Class<? extends Record> recordClazz) throws IllegalAccessException, NoSuchMethodException {
        var lookup = MethodHandles.lookup();
        var hidden = lookup.defineHiddenClass(emitClassBytes(recordClazz), true);
        var clazz = hidden.lookupClass();
        var methodType = MethodType.methodType(Object.class, ByteBuffer.class);

        var loadHandle = hidden.findStatic(clazz, "loadFromBuffer", methodType);
        var writeHandle = hidden.findStatic(clazz, "writeToBuffer", methodType);

        return new BundledInfo(writeHandle, loadHandle, MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("net.ansinn.ByteBarista", clazz.getSimpleName() + "_Codec"),
                "loadFromBuffer_Unsafe",
                MethodTypeDesc.ofDescriptor("(Ljava.nio.ByteBuffer;)L" + clazz.getName())
        ));
    }

    private static byte[] emitClassBytes(Class<? extends Record> clazz) {
        var desc = ClassDesc.of("net.ansinn.ByteBarista", clazz.getSimpleName() + "_Codec");

        return ClassFile.of().build(
                desc,
                builder -> {
                    builder.withFlags(0);

                    var methodType = MethodTypeDesc.of(ConstantDescs.CD_Object, CodegenConstants.BUFFER_DESC);

                    builder.withMethod("loadFromBuffer", methodType, ClassFile.ACC_STATIC, methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> BufferDecoderBuilder.emitReadFunction(codeBuilder, clazz));
                    });

                    builder.withMethod("writeToBuffer", methodType, ClassFile.ACC_STATIC, methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> BufferEncoderBuilder.emitWriteFunction(codeBuilder, clazz));
                    });
                }
        );
    }


    public record BundledInfo(MethodHandle safe, MethodHandle unsafe, MethodHandleDesc description) { }
}