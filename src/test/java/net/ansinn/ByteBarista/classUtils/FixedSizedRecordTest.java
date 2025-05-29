package net.ansinn.ByteBarista.classUtils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static net.ansinn.ByteBarista.ClassUtils.isFixedSize;

public class FixedSizedRecordTest {

    // A simple enum for testing enum handling
    private enum SampleEnum { A, B, C }

    // 1) Record with only primitive fields → fixed size
    private record PrimitiveRecord(int i, long l, boolean b, double d) {}

    // 2) Record with an enum field → fixed size
    private record EnumRecord(SampleEnum e) {}

    // 3) Record with a String field → dynamic size
    private record StringRecord(String s) {}

    // 4) Record with a primitive array → dynamic size
    private record IntArrayRecord(int[] arr) {}

    // 5) Record with an enum array → dynamic size
    private record EnumArrayRecord(SampleEnum[] arr) {}

    // 6) Nested fixed-size record
    private record InnerFixed(int x, short y) {}
    private record OuterFixed(InnerFixed inner) {}

    // 7) Nested dynamic record
    private record InnerDynamic(String s) {}
    private record OuterDynamic(InnerDynamic inner) {}

    // 8) Multi-level nested fixed-size (Outer→Inner→Primitives)
    private record Level1(int a) {}
    private record Level2(Level1 l1) {}
    private record Level3(Level2 l2) {}

    // 9) Mixed nested record with both fixed and dynamic components
    private record MixedInner(int i, String s) {}
    private record MixedOuter(MixedInner inner, long l) {}

    @Test
    // 1) Primitives only
    void testPrimitiveOnlyRecordIsFixedSize() {
        assertTrue(isFixedSize(PrimitiveRecord.class),
            "PrimitiveRecord has only primitives and should be fixed-size");
    }

    @Test
    // 2) Enum handled as fixed-size
    void testEnumRecordIsFixedSize() {
        assertTrue(isFixedSize(EnumRecord.class),
            "EnumRecord contains an enum and should be treated as fixed-size");
    }

    @Test
    // 3) String introduces dynamic sizing
    void testStringRecordIsNotFixedSize() {
        assertFalse(isFixedSize(StringRecord.class),
            "StringRecord contains a String and should not be fixed-size");
    }

    @Test
    // 4) Primitive arrays are dynamic
    void testPrimitiveArrayRecordIsNotFixedSize() {
        assertFalse(isFixedSize(IntArrayRecord.class),
            "IntArrayRecord contains a primitive array and should not be fixed-size");
    }

    @Test
    // 5) Enum arrays are dynamic
    void testEnumArrayRecordIsNotFixedSize() {
        assertFalse(isFixedSize(EnumArrayRecord.class),
            "EnumArrayRecord contains an enum array and should not be fixed-size");
    }

    @Test
    // 6) Single-level nested fixed-size record
    void testNestedFixedRecordIsFixedSize() {
        assertTrue(isFixedSize(OuterFixed.class),
            "OuterFixed wraps only fixed-size InnerFixed and should be fixed-size");
    }

    @Test
    // 7) Single-level nested dynamic record
    void testNestedDynamicRecordIsNotFixedSize() {
        assertFalse(isFixedSize(OuterDynamic.class),
            "OuterDynamic wraps InnerDynamic with a String and should not be fixed-size");
    }

    @Test
    // 8) Multi-level nested fixed-size record
    void testMultiLevelNestedFixedRecordIsFixedSize() {
        assertTrue(isFixedSize(Level3.class),
            "Level3 nested through Level2 and Level1 (all primitives) should be fixed-size");
    }

    @Test
    // 9) Mixed nested record with dynamic inner
    void testMixedNestedRecordIsNotFixedSize() {
        assertFalse(isFixedSize(MixedOuter.class),
            "MixedOuter includes MixedInner (with a String) and should not be fixed-size");
    }

}
