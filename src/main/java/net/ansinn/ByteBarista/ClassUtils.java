package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtils {
    // We're going to want to cache commonly used record sizes to cut down on needless sum calls.
    private static final Map<Class<? extends Record>, Integer> SizeCache = new ConcurrentHashMap<>();

    /**
     * Get cumulative size of types within record in bytes from cache or calculate it anew.
     *
     * @param recordClazz record to read
     * @return number of bytes in record
     */
    public static int getRecordSize(final Class<? extends Record> recordClazz) {
        SizeCache.computeIfAbsent(recordClazz, _ -> sumFieldSizes(recordClazz.getRecordComponents()));
        return SizeCache.get(recordClazz);
    }

    /**
     * Sum sizes of fixed sized variables to assist in faster computation of variables.
     * Enums *are* also allowed on the condition that there's less than 255 enum ordinals.
     *
     * @param components component fields to be summed up
     * @return size of object fields
     */
    private static int sumFieldSizes(final RecordComponent[] components) {
        return Arrays.stream(components).mapToInt(field -> field.getType().isEnum()
                ? Byte.BYTES : switch (field.getType().getTypeName()) {
            case "long" -> {
                if (field.isAnnotationPresent(UnsignedByte.class))
                    yield Byte.BYTES;
                else if (field.isAnnotationPresent(UnsignedShort.class))
                    yield Short.BYTES;
                else if (field.isAnnotationPresent(UnsignedInteger.class))
                    yield Integer.BYTES;
                yield Long.BYTES;
            }
            case "int" -> {
                if (field.isAnnotationPresent(UnsignedByte.class))
                    yield Byte.BYTES;
                else if (field.isAnnotationPresent(UnsignedShort.class))
                    yield Short.BYTES;
                yield Integer.BYTES;
            }
            case "short" -> Short.BYTES;
            case "byte" -> Byte.BYTES;

            case "double" -> Double.BYTES;
            case "float" -> Float.BYTES;

            case "char" -> Character.BYTES;

            case "boolean" -> throw new IllegalStateException("Type 'Boolean' is not a permitted value");
            default -> throw new IllegalStateException("Unexpected value: " + field.getType().getTypeName());
        }).sum();
    }

    /**
     * Detects whether a record is infinitely nested, i.e., contains a direct or indirect cycle
     * of record components referencing itself or each other.
     * <p>
     * This is a conservative check that assumes all record components are non-nullable. It will treat
     * any cyclic structure (e.g. {@code record A(B b)}, {@code record B(A a)}) as an infinite nesting pattern.
     * <p>
     * In the future, if Java introduces official nullability support,
     * this method may be revised to account for potentially-null references, allowing for legitimate
     * recursive structures like trees or linked lists where components may be {@code null}.
     * <p>
     * For now, this method errs on the side of safety and disallows recursive nesting unless clearly finite.
     *
     * @param clazz the class to analyze (must be a record)
     * @return {@code true} if the record structure is cyclic; {@code false} otherwise
     */
    public static <T extends Record> boolean isInfinitelyNested(Class<T> recordClazz) {
        return isInfinitelyNested(recordClazz, new HashSet<>());
    }

    private static boolean isInfinitelyNested(Class<?> recordClazz, Set<Class<?>> visitedClasses) {
        if (!visitedClasses.add(recordClazz)) return true; // cycle detected

        for (RecordComponent component : recordClazz.getRecordComponents()) {
            Class<?> type = component.getType();

            // Only recurse into nested records
            if (type.isPrimitive() || type.isEnum() || type == String.class)
                continue;

            // TODO: When nullability annotations or value constraints exist, refine this logic
            if (isInfinitelyNested(type, visitedClasses))
                return true;
        }

        visitedClasses.remove(recordClazz);
        return false;
    }

    public static <T extends Record> boolean isFixedSize(Class<T> recordClazz) {

    }

    /**
     * Returns the JVM descriptor string for the given record component's type.
     * <p>
     * This method handles all primitive types supported by Java records, as well as reference types.
     * For primitive types, it returns the corresponding single-letter descriptor (e.g., {@code "I"} for {@code int}).
     * For reference types, it returns a descriptor of the form {@code "Lfully/qualified/ClassName;"}.
     * <p>
     * Unsupported types such as {@code boolean} and {@code void} will result in an {@link IllegalArgumentException}.
     *
     * @param component the {@link RecordComponent} whose type descriptor should be emitted
     * @return the JVM descriptor string for the component's type
     * @throws IllegalArgumentException if the type is {@code boolean}, {@code void}, or otherwise unsupported
     */
    public static String getDescriptor(RecordComponent component) {
        var type = component.getType();

        if (type.isPrimitive()) {
            return switch (type.getName()) {
                case "long" -> "J";
                case "int" -> "I";
                case "short" -> "S";
                case "byte" -> "B";

                case "double" -> "D";
                case "float" -> "F";

                case "char" -> "C";

                case "boolean" -> throw new IllegalArgumentException("Type boolean is currently unsupported.");
                case "void" -> throw new IllegalArgumentException("Type void is not permitted within record encoders and decoders.");
                default -> throw new IllegalArgumentException("Unknown primitive: " + type);
            };
        } else return "L" + type.getName() + ";";
    }
}
