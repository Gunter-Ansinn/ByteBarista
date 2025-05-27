package net.ansinn.ByteBarista.codecs;

import java.nio.ByteBuffer;

public sealed interface Codec<T extends Record> permits DynamicCodec, FixedCodec {
    T decode(ByteBuffer buffer);
    void encode(ByteBuffer buffer, T value);
}
