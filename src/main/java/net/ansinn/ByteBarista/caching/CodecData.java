package net.ansinn.ByteBarista.caching;

public record CodecData(Class<?> clazz, long versionHash, byte[] clazzData) {
}
