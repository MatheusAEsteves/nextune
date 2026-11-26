package com.nextune.common.util;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Converte UUID para os 16 bytes gravados nas colunas BINARY(16) (mesma ordem usada pelo Hibernate).
 * Usado em queries nativas de escrita, para não depender de como o driver converte UUID.
 */
public final class UuidBytes {

    private UuidBytes() {}

    public static byte[] de(UUID uuid) {
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }
}
