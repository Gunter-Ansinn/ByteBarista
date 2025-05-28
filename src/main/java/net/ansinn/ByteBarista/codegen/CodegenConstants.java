package net.ansinn.ByteBarista.codegen;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

public final class CodegenConstants {

    private CodegenConstants() {}

    public static final ClassDesc BUFFER_DESC = ClassDesc.of("java.nio.ByteBuffer");
    public static final ClassDesc HELPER_DESC = ClassDesc.of("net.ansinn.ByteBarista.NumericHelpers");

    public static final MethodTypeDesc BUFFER_LONG_LOAD = MethodTypeDesc.ofDescriptor("(Ljava.nio.ByteBuffer;)J");
    public static final MethodTypeDesc BUFFER_INT_LOAD = MethodTypeDesc.ofDescriptor("(Ljava.nio.ByteBuffer;)I");

    public static final MethodTypeDesc INT_DESC = MethodTypeDesc.ofDescriptor("()I");
    public static final MethodTypeDesc SHORT_DESC = MethodTypeDesc.ofDescriptor("()S");
    public static final MethodTypeDesc BYTE_DESC = MethodTypeDesc.ofDescriptor("()B");

}