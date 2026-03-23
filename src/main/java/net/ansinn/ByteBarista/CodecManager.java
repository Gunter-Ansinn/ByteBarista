package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.codecs.Codec;
import net.ansinn.ByteBarista.codecs.DynamicCodec;
import net.ansinn.ByteBarista.codecs.FixedCodec;
import net.ansinn.ByteBarista.codegen.RecordCodecBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Codec manager organizes coders and encoders into a unified Codec class which wraps up the decode and encode
 * operations into a singular unified
 *
 * @author Gunter Ansinn
 */
public class CodecManager {

    private static ConcurrentMap<Class<? extends Record>, Codec<? extends Record>> CODEC_CACHE = new ConcurrentHashMap<>();

    public static <T extends Record> T getCodec(Class<T> codecType) {
        Objects.requireNonNull(codecType, "Codec type key cannot be null.");

        if (ClassUtils.isInfinitelyNested(codecType))
            throw new IllegalStateException("This record codec is invalid as it infinitely nests itself or subclasses.");

        return null;
    }

    public static <T extends Record> FixedCodec<T> getSafe(Class<T> codecType) {
        return null;
    }

    public static <T extends Record> Optional<FixedCodec<T>> getSafeOptionally(Class<T> codecType) {
        return Optional.empty();
    }

    public static <T extends Record> DynamicCodec<T> getUnsafe(Class<T> codecType) {
        return null;
    }

}
