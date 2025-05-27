package net.ansinn.ByteBarista.codecs;

import net.ansinn.ByteBarista.ClassUtils;

import java.nio.ByteBuffer;

public record DynamicCodec<T extends Record>() implements Codec<T> {
    @Override
    public T decode(ByteBuffer buffer) {
        return null;
    }

    @Override
    public void encode(ByteBuffer buffer, T value) {

    }
}
