package net.ansinn.ByteBarista.codegen;

import java.lang.classfile.CodeBuilder;
import java.lang.reflect.RecordComponent;

/**
 * Utility class for typed JVM bytecode operations related to loading and storing
 * primitive record components. This class provides helpers to emit the correct
 * {@link CodeBuilder} instructions for primitive types when working with local
 * variable slots and stack-based construction.
 *
 * <p>
 * This utility is intended for use within code generation logic such as stream
 * and buffer decoder builders, where JVM operand stack behavior must be
 * accurately reflected for each primitive type.
 *
 * @author Gunter Ansinn
 */
public final class DecoderUtils {

    private DecoderUtils() {}

    /**
     * Emits the appropriate JVM bytecode instruction to load a value from a local
     * variable slot based on the type of the given record component.
     *
     * @param builder the {@link CodeBuilder} used to emit bytecode
     * @param component the {@link RecordComponent} whose type determines the load instruction
     * @param index the local variable slot index
     */
    public static void loadType(CodeBuilder builder, RecordComponent component, int index) {
        var type = component.getType();

        if (type == long.class)
            builder.lload(index);
        else if (type == double.class)
            builder.dload(index);
        else if (type == float.class)
            builder.fload(index);
        else
            builder.iload(index);
    }

    /**
     * Emits the appropriate JVM bytecode instruction to store a value into a local
     * variable slot based on the type of the given record component.
     *
     * @param builder the {@link CodeBuilder} used to emit bytecode
     * @param component the {@link RecordComponent} whose type determines the store instruction
     * @param index the local variable slot index
     */
    public static void storeType(CodeBuilder builder, RecordComponent component, int index) {
        var type = component.getType();

        if (type == long.class)
            builder.lstore(index);
        else if (type == double.class)
            builder.dstore(index);
        else if (type == float.class)
            builder.fstore(index);
        else
            builder.istore(index);
    }

    /**
     * Returns the number of local variable slots required to store the value
     * of the given record component. This accounts for wide primitives such as
     * {@code long} and {@code double}, which occupy two slots in the JVM's
     * local variable table.
     *
     * @param component the {@link RecordComponent} to inspect
     * @return {@code 2} if the type is {@code long} or {@code double}, otherwise {@code 1}
     */
    public static int slotsFor(RecordComponent component) {
        var type = component.getType();

        if (type == long.class || type == double.class)
            return 2;
        return 1;
    }

}
