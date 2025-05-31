package net.ansinn.ByteBarista.codecs;

import net.ansinn.ByteBarista.ClassUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandle;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/**
 * A {@code DynamicCodec} is a flexible, low-level codec implementation that allows for encoding and decoding
 * of {@link Record} types without any guarantees of fixed size or preconditions on input buffers or streams.
 * <p>
 * Unlike {@link FixedCodec}, this codec does not enforce any runtime checks for buffer bounds or stream length.
 * It is intended for use in performance-critical scenarios or where the structure of the record does not allow
 * for a statically known fixed size.
 * <p>
 * The decoding and encoding operations are backed by {@link MethodHandle} instances generated at runtime.
 * If these handles fail, it indicates a bug in the code generation process rather than user error.
 * <p>
 * As this codec performs no size validation, callers are responsible for ensuring that input buffers and
 * streams contain enough data to decode the record, and that output targets can accept the required size.
 * Failures during invocation may result in unchecked exceptions such as {@link BufferUnderflowException}.
 *
 * @param <T> the record type this codec operates on
 * @see FixedCodec
 * @see net.ansinn.ByteBarista.CodecManager#getUnsafe(Class)
 * @see Codec
 * @author Gunter Ansinn
 */
public record DynamicCodec<T extends Record>(MethodHandle bufferDecode, MethodHandle streamDecode, MethodHandle bufferEncode, MethodHandle streamEncode) implements Codec<T> {

    /**
     * Decodes a record from the provided {@link ByteBuffer} without performing any safety checks.
     * <p>
     * This method assumes the buffer contains enough data to decode the record as defined by the
     * underlying {@link MethodHandle}. If insufficient data is available, a
     * {@link java.nio.BufferUnderflowException} may be thrown.
     *
     * @param buffer the {@link ByteBuffer} containing the encoded record data
     * @return the decoded record of type {@code T}
     * @throws BufferUnderflowException if the buffer does not contain enough data
     * @throws IllegalStateException if the decoding logic fails due to internal code generation errors
     */
    @Override
    @SuppressWarnings("unchecked")
    public T decode(ByteBuffer buffer) {
        try {
            return (T) bufferDecode().invokeExact(buffer);
        } catch (BufferUnderflowException exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unexpected failure in Dynamic Codec. This indicates an error in generated code.");
        }
    }

    /**
     * Decodes a record from the provided {@link ByteArrayInputStream} without performing any safety checks.
     * <p>
     * It is the caller's responsibility to ensure that the stream contains enough data.
     * Any failure during decoding is considered a bug in the internal code generation process.
     *
     * @param inputStream the {@link ByteArrayInputStream} containing the encoded record data
     * @return the decoded record of type {@code T}
     * @throws IllegalStateException if the decoding logic fails due to internal code generation errors
     */
    @Override
    @SuppressWarnings("unchecked")
    public T decode(ByteArrayInputStream inputStream) {
        try {
            return (T) streamDecode().invokeExact(inputStream);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unexpected failure in Dynamic Codec. This indicates an error in generated code.");
        }
    }

    /**
     * Encodes the given record into the provided {@link ByteBuffer} without performing any safety checks.
     * <p>
     * The buffer must have sufficient remaining capacity to accept the encoded data. If not,
     * this method may throw a {@link java.nio.BufferOverflowException}.
     *
     * @param buffer the {@link ByteBuffer} to write the encoded record to
     * @param value the record instance to encode
     * @throws BufferOverflowException if the buffer does not have enough space
     * @throws ReadOnlyBufferException if the buffer is read-only
     * @throws IllegalStateException if the encoding logic fails due to internal code generation errors
     */
    @Override
    public void encode(ByteBuffer buffer, T value) {
        try {
            bufferEncode().invokeExact(buffer, value);
        } catch (BufferOverflowException | ReadOnlyBufferException exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unexpected failure in Dynamic Codec. This indicates an error in generated code.");
        }
    }

    /**
     * Encodes the given record into the provided {@link ByteArrayOutputStream} without performing any safety checks.
     * <p>
     * This method assumes that the stream is writable and will dynamically grow as needed.
     * Any failures are treated as internal codec generation bugs.
     *
     * @param outputStream the {@link ByteArrayOutputStream} to write the encoded record to
     * @param value the record instance to encode
     * @throws IllegalStateException if the encoding logic fails due to internal code generation errors
     */
    @Override
    public void encode(ByteArrayOutputStream outputStream, T value) {
        try {
            streamDecode().invokeExact(outputStream, value);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unexpected failure in Dynamic Codec. This indicates an error in generated code.");
        }
    }
}
