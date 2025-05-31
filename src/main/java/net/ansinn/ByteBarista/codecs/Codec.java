package net.ansinn.ByteBarista.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public sealed interface Codec<T extends Record> permits DynamicCodec, FixedCodec {
    T decode(ByteBuffer buffer);
    T decode(ByteArrayInputStream inputStream);

    void encode(ByteBuffer buffer, T value);
    void encode(ByteArrayOutputStream outputStream, T value);
}
