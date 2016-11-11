package com.benromberg.cordonbleu.data.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class RandomIdGenerator {
    public static String generate() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidAsBytes = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits()).array();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidAsBytes);
    }
}
