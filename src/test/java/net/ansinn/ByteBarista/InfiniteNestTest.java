package net.ansinn.ByteBarista;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InfiniteNestTest {

    @Test
    void testFlatRecord() {
        assertFalse(ClassUtils.isInfinitelyNested(Simple.class), "Simple should not be considered infinitely nested.");
    }

    @Test
    void testDirectSelfReference() {
        assertTrue(ClassUtils.isInfinitelyNested(DirectSelf.class), "DirectSelf should be considered infinitely nested.");
    }

    @Test
    void testIndirectCycle() {
        assertTrue(ClassUtils.isInfinitelyNested(IndirectA.class), "IndirectA should be considered infinitely nested.");
        assertTrue(ClassUtils.isInfinitelyNested(IndirectB.class), "IndirectB should be considered infinitely nested.");
        assertTrue(ClassUtils.isInfinitelyNested(IndirectC.class), "IndirectC should be considered infinitely nested.");
    }

    @Test
    void testBinaryTreeStyle() {
        assertTrue(ClassUtils.isInfinitelyNested(Tree.class), "Tree should be considered infinitely nested.");
    }

    @Test
    void testLinkedListStyle() {
        assertTrue(ClassUtils.isInfinitelyNested(LinkedList.class), "LinkedList should be considered infinitely nested.");
    }

    @Test
    void testNullableStyleCycle() {
        assertTrue(ClassUtils.isInfinitelyNested(NullableStyle.class), "NullableStyle should be considered infinitely nested.");
    }

    @Test
    void testFlatReference() {
        assertFalse(ClassUtils.isInfinitelyNested(FlatReference.class), "FlatReference should not be considered infinitely nested.");
    }

    @Test
    void testMixedNested() {
        assertTrue(ClassUtils.isInfinitelyNested(Mixed.class), "Mixed should be considered infinitely nested.");
    }

    public record Simple(int x, float y) {}

    public record IndirectA(IndirectB b) {}
    public record IndirectB(IndirectC c) {}
    public record IndirectC(IndirectA a) {}

    public record DirectSelf(DirectSelf self) {}

    public record NullableStyle(NullableStyle next) {}

    public record Tree(Tree left, Tree right) {}

    public record LinkedList(LinkedList next) {}

    public record FlatReference(Simple other) {}

    public record Mixed(Simple simple, DirectSelf self) {}
}
