/**
 * Copyright (C) 2010, FuseSource Corp.  All rights reserved.
 */
package org.fusesource.hawtbuf;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class AbstractVarIntSupport {

    abstract protected byte readByte() throws IOException;

    abstract protected void writeByte(int value) throws IOException;

    /**
     * Read a raw Varint from the stream. If larger than 32 bits, discard the
     * upper bits.
     */
    public int readVarInt() throws IOException {
        byte tmp = readByte();
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = readByte()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = readByte()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = readByte()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = readByte()) << 28;
                    if (tmp < 0) {
                        // Discard upper 32 bits.
                        for (int i = 0; i < 5; i++) {
                            if (readByte() >= 0)
                                return result;
                        }
                        throw new ProtocolException("Encountered a malformed variable int");
                    }
                }
            }
        }
        return result;
    }

    /** Read a raw Varint from the stream. */
    public long readVarLong() throws IOException {
        int shift = 0;
        long result = 0;
        while (shift < 64) {
            byte b = readByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0)
                return result;
            shift += 7;
        }
        throw new ProtocolException("Encountered a malformed variable int");
    }


    /** Read an {@code sint32} field value from the stream. */
    public int readVarSignedInt() throws IOException {
        return decodeZigZag32(readVarInt());
    }

    /** Read an {@code sint64} field value from the stream. */
    public long readVarSignedLong() throws IOException {
        return decodeZigZag64(readVarLong());
    }

    /**
     * Encode and write a varint. {@code value} is treated as unsigned, so it
     * won't be sign-extended if negative.
     */
    public void writeVarInt(int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                writeByte(value);
                return;
            } else {
                writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }


    /** Encode and write a varint. */
    public void writeVarLong(long value) throws IOException {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                writeByte((int) value);
                return;
            } else {
                writeByte(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }


    public void writeVarSignedInt(int value) throws IOException {
        writeVarInt(encodeZigZag32(value));
    }

    public void writeVarSignedLong(long value) throws IOException {
        writeVarLong(encodeZigZag64(value));
    }

    private static int decodeZigZag32(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private static long decodeZigZag64(long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private static int encodeZigZag32(int n) {
        return (n << 1) ^ (n >> 31);
    }

    private static long encodeZigZag64(long n) {
        return (n << 1) ^ (n >> 63);
    }

    /**
     * Compute the number of bytes that would be needed to encode a varint.
     * {@code value} is treated as unsigned, so it won't be sign-extended if
     * negative.
     */
    public static int computeVarIntSize(int value) {
        if ((value & (0xffffffff << 7)) == 0)
            return 1;
        if ((value & (0xffffffff << 14)) == 0)
            return 2;
        if ((value & (0xffffffff << 21)) == 0)
            return 3;
        if ((value & (0xffffffff << 28)) == 0)
            return 4;
        return 5;
    }

    /** Compute the number of bytes that would be needed to encode a varint. */
    public static int computeVarLongSize(long value) {
        if ((value & (0xffffffffffffffffL << 7)) == 0)
            return 1;
        if ((value & (0xffffffffffffffffL << 14)) == 0)
            return 2;
        if ((value & (0xffffffffffffffffL << 21)) == 0)
            return 3;
        if ((value & (0xffffffffffffffffL << 28)) == 0)
            return 4;
        if ((value & (0xffffffffffffffffL << 35)) == 0)
            return 5;
        if ((value & (0xffffffffffffffffL << 42)) == 0)
            return 6;
        if ((value & (0xffffffffffffffffL << 49)) == 0)
            return 7;
        if ((value & (0xffffffffffffffffL << 56)) == 0)
            return 8;
        if ((value & (0xffffffffffffffffL << 63)) == 0)
            return 9;
        return 10;
    }

    /**
     * Compute the number of bytes that would be needed to encode a signed varint.
     */
    public static int computeVarSignedIntSize(int value) {
        return computeVarIntSize(encodeZigZag32(value));
    }

    /**
     * Compute the number of bytes that would be needed to encode a signed varint.
     */
    public static int computeVarSignedLongSize(long value) {
        return computeVarLongSize(encodeZigZag64(value));
    }
}
