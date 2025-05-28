package net.ansinn.ByteBarista;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.ansinn.ByteBarista.codegen.RecordCodecBuilder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecoderClassBuilderTest {

    @Test
    void test() throws Throwable {
        var info = bundleInfo(Normal.class);
        var target = new Normal(12345, (byte) 42);
        var buffer = ByteBuffer.allocate(5);

        buffer.putInt(target.foo);
        buffer.put(target.bar);
        buffer.flip();

        var result = info.safe().invoke(buffer);

        assertEquals(target, result, "target record didn't match result record");
        System.out.println("result = " + result.toString());
    }

    @Test
    void testLocal() throws Throwable {
        record Local(int a, int b, int c) {}

        var info = bundleInfo(Local.class);
        var target = new Local(4, 3, 64);
        var buffer = ByteBuffer.allocate(4 * 3);

        buffer.putInt(target.a);
        buffer.putInt(target.b);
        buffer.putInt(target.c);
        buffer.flip();

        var result = info.safe().invoke(buffer);

        assertEquals(target, result);
        System.out.println("result = " + result.toString());
    }

    @Test
    void testNested() throws Throwable {
        var info = bundleInfo(Nested.class);
        var target = new Nested(
                new Position3D(0.0f, 0.0f, 0.0f),
                new Position3D(2.0f, 2.0f, 2.0f)
        );
        var buffer = ByteBuffer.allocate(Float.BYTES * 6);

        buffer.putFloat(target.min.x);
        buffer.putFloat(target.min.y);
        buffer.putFloat(target.min.z);

        buffer.putFloat(target.max.x);
        buffer.putFloat(target.max.y);
        buffer.putFloat(target.max.z);
        buffer.flip();

        var result = info.safe().invoke(buffer);
        assertEquals(target, result);
        System.out.println("result = " + result.toString());
    }


    record Normal(int foo, byte bar) { }

    record Nested(Position3D min, Position3D max) {}
    record Position3D(float x, float y, float z) {}
}
