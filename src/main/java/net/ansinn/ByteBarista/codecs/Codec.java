package net.ansinn.ByteBarista.codecs;

import java.nio.ByteBuffer;

public sealed interface Codec<T> permits SafeCodec, DynamicCodec {
    T decode(ByteBuffer buffer);
    void encode(ByteBuffer buffer, T value);
}
