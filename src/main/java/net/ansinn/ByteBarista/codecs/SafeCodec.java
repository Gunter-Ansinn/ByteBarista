package net.ansinn.ByteBarista.codecs;

import java.nio.ByteBuffer;

public record SafeCodec<T>() implements Codec<T> {

    @Override
    public T decode(ByteBuffer buffer) {
        return null;
    }

    @Override
    public void encode(ByteBuffer buffer, T value) {

    }
}
