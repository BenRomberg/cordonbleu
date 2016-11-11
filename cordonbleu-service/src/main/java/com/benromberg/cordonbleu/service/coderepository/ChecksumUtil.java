package com.benromberg.cordonbleu.service.coderepository;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

public class ChecksumUtil {
    public static String stringToMd5HexChecksum(String source) {
        return digestToHex(createMd5Digest().digest(source.getBytes()));
    }

    public static String inputStreamToMd5HexChecksum(InputStream inputStream) throws IOException {
        MessageDigest messageDigest = createMd5Digest();
        try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {
            while (digestInputStream.read() >= 0) {
            }
        }
        return digestToHex(messageDigest.digest());
    }

    private static String digestToHex(byte[] digest) {
        return Hex.encodeHexString(digest);
    }

    private static MessageDigest createMd5Digest() {
        return convertException(() -> MessageDigest.getInstance("MD5"));
    }
}
