package net.ansinn.ByteBarista.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * A {@code FixedCodec} is a safety-enforcing wrapper around a {@link DynamicCodec}
 * that guarantees all encoded and decoded data has a statically known fixed size.
 * <p>
 * This codec performs runtime pre-checks on {@link ByteBuffer} and {@link ByteArrayInputStream}
 * to ensure that sufficient data is available for decoding and that adequate space is available
 * when encoding. These checks prevent buffer underflows and overflows, providing a level of safety
 * not afforded by {@link DynamicCodec}.
 * <p>
 * A {@code FixedCodec} is typically obtained from {@code CodecManager.getSafe(Class<T>)} or
 * {@code CodecManager.getSafeOptionally(Class<T>)}. If a class's size cannot be statically determined,
 * no {@code FixedCodec} will be available.
 * <p>
 * Note: This codec should be preferred when the target record has a layout that can be fully
 * described with fixed-size fields and nested {@code FixedCodec}s. For dynamic or variably-sized
 * types, fall back to {@link DynamicCodec}.
 *
 * @param <T> the record type this codec operates on
 * @see DynamicCodec
 * @see net.ansinn.ByteBarista.CodecManager
 * @author Gunter Ansinn
 */
public record FixedCodec<T extends Record>(int size, DynamicCodec<T> internalCodec) implements Codec<T> {

    /**
     * Decodes a record from the provided {@link ByteBuffer}.
     * <p>
     * This method checks that there is sufficient data remaining in the buffer to
     * decode the record. If the buffer's remaining data is smaller than the codec's
     * fixed size, an {@link IllegalStateException} will be thrown.
     *
     * @param buffer the {@link ByteBuffer} from which the record will be decoded
     * @return the decoded record of type {@code T}
     * @throws IllegalStateException if the buffer's remaining data is smaller than the codec's size
     */
    @Override
    public T decode(ByteBuffer buffer) {
        if (buffer.remaining() < size())
            throw new IllegalStateException("Attempt to read from buffer smaller than codec data.");
        return internalCodec.decode(buffer);
    }

    /**
     * Decodes a record from the provided {@link ByteArrayInputStream}.
     * <p>
     * This method checks that there are enough bytes available in the stream to
     * decode the record. If the stream does not have enough data, an
     * {@link IllegalStateException} will be thrown.
     *
     * @param stream the {@link ByteArrayInputStream} from which the record will be decoded
     * @return the decoded record of type {@code T}
     * @throws IllegalStateException if the stream does not have enough available bytes to decode the record
     */
    @Override
    public T decode(ByteArrayInputStream stream) {
        if (stream.available() < size())
            throw new IllegalStateException("Attempt to read from byte array input stream without enough elements for codec.");
        return internalCodec().decode(stream);
    }

    /**
     * Encodes the given record into the provided {@link ByteBuffer}.
     * <p>
     * This method checks that there is sufficient space in the buffer to write
     * the encoded record. If there is not enough space remaining, an
     * {@link IllegalStateException} will be thrown.
     *
     * @param buffer the {@link ByteBuffer} where the record will be encoded
     * @param value the record of type {@code T} to encode
     * @throws IllegalStateException if the buffer does not have enough space to encode the record
     */
    @Override
    public void encode(ByteBuffer buffer, T value) {
        if (buffer.remaining() < size)
            throw new IllegalStateException("Attempt to write to byte buffer without sufficient space remaining.");
        internalCodec().encode(buffer, value);
    }

    /**
     * Encodes the given record into the provided {@link ByteArrayOutputStream}.
     * <p>
     * This method will write the encoded record to the output stream. There are no size checks
     * for the output stream, as it can grow dynamically to accommodate the encoded data.
     *
     * @param outputStream the {@link ByteArrayOutputStream} where the record will be encoded
     * @param value the record of type {@code T} to encode
     */
    @Override
    public void encode(ByteArrayOutputStream outputStream, T value) {
        internalCodec().encode(outputStream, value);
    }
}
