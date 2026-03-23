package net.ansinn.ByteBarista.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A generic serialization interface for encoding and decoding {@link Record} types
 * to and from binary representations using {@link ByteBuffer} and {@link ByteArrayInputStream}.
 * <p>
 * Implementations of this interface may or may not enforce strict size or safety guarantees.
 * Use {@link FixedCodec} for size-verified serialization, or {@link DynamicCodec} for
 * performance-oriented, unsafe operations.
 * <p>
 * This interface allows for pluggable codec strategies that can be dynamically selected,
 * pattern-matched, or composed based on runtime requirements.
 *
 * @param <T> the record type handled by this codec
 * @see FixedCodec
 * @see DynamicCodec
 * @see net.ansinn.ByteBarista.CodecManager
 * @author Gunter Ansinn
 */
public sealed interface Codec<T extends Record> permits DynamicCodec, FixedCodec {

    /**
     * Decodes a record from the given {@link ByteBuffer}.
     * <p>
     * Implementations may throw a runtime exception if the buffer does not contain
     * sufficient data or is otherwise invalid for the record structure.
     *
     * @param buffer the binary data source
     * @return the decoded record instance
     */
    T decode(ByteBuffer buffer);

    /**
     * Decodes a record from the given {@link ByteArrayInputStream}.
     * <p>
     * Behavior and error handling may vary depending on whether the codec implementation
     * enforces size safety.
     *
     * @param inputStream the input stream containing record bytes
     * @return the decoded record instance
     */
    T decode(ByteArrayInputStream inputStream);

    /**
     * Encodes the given record into the specified {@link ByteBuffer}.
     * <p>
     * Implementations may throw if the buffer does not have sufficient space.
     *
     * @param buffer the target buffer for binary encoding
     * @param value the record to encode
     */
    void encode(ByteBuffer buffer, T value);

    /**
     * Encodes the given record into the specified {@link ByteArrayOutputStream}.
     *
     * @param outputStream the output stream to write binary data into
     * @param value the record to encode
     */
    void encode(ByteArrayOutputStream outputStream, T value);
}
