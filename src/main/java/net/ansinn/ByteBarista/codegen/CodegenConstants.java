package net.ansinn.ByteBarista.codegen;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ConstantDescs;

public final class CodegenConstants {

    private CodegenConstants() {}

    // PRIMITIVE wrappers
    public static final ClassDesc DOUBLE_DESC = ClassDesc.of("java.lang.Double");
    public static final ClassDesc FLOAT_DESC = ClassDesc.of("java.lang.Float");


    // TYPE WRAPPERS
    public static final ClassDesc BUFFER_DESC = ClassDesc.of("java.nio.ByteBuffer");
    public static final ClassDesc HELPER_DESC = ClassDesc.of("net.ansinn.ByteBarista.NumericHelpers");
    public static final ClassDesc INPUT_DESC = ClassDesc.of("java.io.ByteArrayInputStream");

    public static final MethodTypeDesc BUFFER_LONG_LOAD = MethodTypeDesc.of(ConstantDescs.CD_long, BUFFER_DESC);
    public static final MethodTypeDesc BUFFER_INT_LOAD = MethodTypeDesc.of(ConstantDescs.CD_int, BUFFER_DESC);

    public static final MethodTypeDesc INT_DESC = MethodTypeDesc.ofDescriptor("()I");
    public static final MethodTypeDesc SHORT_DESC = MethodTypeDesc.ofDescriptor("()S");
    public static final MethodTypeDesc BYTE_DESC = MethodTypeDesc.ofDescriptor("()B");

}