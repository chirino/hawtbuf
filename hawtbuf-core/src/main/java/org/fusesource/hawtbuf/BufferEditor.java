/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtbuf;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * <p>
 * Used to write and read primitives to and from a Buffer.  Reads and
 * writes are done at the buffers offset.  Every read and write
 * increases the buffer's offset and decreases the buffer's length.
 * </p>
 * <p>
 * Bounds checking are only performed when assertions are enabled on
 * the JVM.  It's up to you to make sure there is enough data/space
 * in the buffer to do the read or write.
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class BufferEditor {
    protected final Buffer buffer;

    private BufferEditor(Buffer buffer) {
        this.buffer = buffer;
    }

    protected boolean hasCapacity(int len) {
        return buffer.length >= len;
    }

    public int read() {
        buffer.length--;
        return buffer.data[buffer.offset++] & 0xff;
    }

    public void readFully(byte[] b) {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) {
        assert (hasCapacity(len));
        System.arraycopy(buffer.data, buffer.offset, b, off, len);
        buffer.offset += len;
        buffer.length -= len;
    }

    public int skipBytes(int n) {
        int len = Math.min(n, buffer.length);
        buffer.offset += len;
        buffer.length -= len;
        return len;
    }

    public boolean readBoolean() {
        assert (hasCapacity(1));
        return read() != 0;
    }

    public byte readByte() {
        assert (hasCapacity(1));
        return (byte) read();
    }

    public int readUnsignedByte() {
        assert (hasCapacity(1));
        return read();
    }

    public void write(int b) {
        assert (hasCapacity(1));
        buffer.data[buffer.offset++] = (byte) b;
        buffer.length--;
    }

    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) {
        assert (hasCapacity(len));
        System.arraycopy(b, off, buffer.data, buffer.offset, len);
        buffer.offset += len;
        buffer.length -= len;
    }

    public void writeBoolean(boolean v) {
        assert (hasCapacity(1));
        write(v ? 1 : 0);
    }

    public void writeByte(int v) {
        assert (hasCapacity(1));
        write(v);
    }

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

    private static int decodeZigZag32(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private static long decodeZigZag64(long n) {
        return (n >>> 1) ^ -(n & 1);
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

    private static int encodeZigZag32(int n) {
        return (n << 1) ^ (n >> 31);
    }

    private static long encodeZigZag64(long n) {
        return (n << 1) ^ (n >> 63);
    }

    abstract public short readShort();

    abstract public int readUnsignedShort();

    abstract public char readChar();

    abstract public int readInt();

    abstract public long readLong();

    abstract public double readDouble();

    abstract public float readFloat();

    abstract public void writeShort(int v);

    abstract public void writeChar(int v);

    abstract public void writeInt(int v);

    abstract public void writeLong(long v);

    abstract public void writeDouble(double v);

    abstract public void writeFloat(float v);

    abstract public void writeRawDouble(double v);

    abstract public void writeRawFloat(float v);

    static class BigEndianBufferEditor extends BufferEditor {

        BigEndianBufferEditor(Buffer buffer) {
            super(buffer);
        }

        public short readShort() {
            assert (hasCapacity(2));
            return (short) ((read() << 8) + (read() << 0));
        }


        public int readUnsignedShort() {
            assert (hasCapacity(2));
            return (read() << 8) + (read() << 0);
        }

        public char readChar() {
            assert (hasCapacity(2));
            return (char) ((read() << 8) + (read() << 0));
        }


        public int readInt() {
            assert (hasCapacity(4));
            return (read() << 24) + (read() << 16) + (read() << 8) + (read() << 0);
        }


        public long readLong() {
            assert (hasCapacity(8));
            return ((long) read() << 56) + ((long) read() << 48) + ((long) read() << 40) + ((long) read() << 32) + ((long) read() << 24)
                    + ((read()) << 16) + ((read()) << 8) + ((read()) << 0);
        }


        public double readDouble() {
            return Double.longBitsToDouble(readLong());
        }


        public float readFloat() {
            return Float.intBitsToFloat(readInt());
        }

        public void writeShort(int v) {
            assert (hasCapacity(2));
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }


        public void writeChar(int v) {
            assert (hasCapacity(2));
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }


        public void writeInt(int v) {
            assert (hasCapacity(4));
            write((v >>> 24) & 0xFF);
            write((v >>> 16) & 0xFF);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }

        public void writeLong(long v) {
            assert (hasCapacity(8));
            write((int) (v >>> 56) & 0xFF);
            write((int) (v >>> 48) & 0xFF);
            write((int) (v >>> 40) & 0xFF);
            write((int) (v >>> 32) & 0xFF);
            write((int) (v >>> 24) & 0xFF);
            write((int) (v >>> 16) & 0xFF);
            write((int) (v >>> 8) & 0xFF);
            write((int) (v >>> 0) & 0xFF);
        }


        public void writeDouble(double v) {
            writeLong(Double.doubleToLongBits(v));
        }

        public void writeFloat(float v) {
            writeInt(Float.floatToIntBits(v));
        }

        public void writeRawDouble(double v) {
            writeLong(Double.doubleToRawLongBits(v));
        }

        public void writeRawFloat(float v) {
            writeInt(Float.floatToRawIntBits(v));
        }
    }

    ;


    static class LittleEndianBufferEditor extends BufferEditor {

        LittleEndianBufferEditor(Buffer buffer) {
            super(buffer);
        }

        public short readShort() {
            assert (hasCapacity(2));
            return (short) ((read() << 0) + (read() << 8));
        }

        public int readUnsignedShort() {
            assert (hasCapacity(2));
            return (read() << 0) + (read() << 8);
        }

        public char readChar() {
            assert (hasCapacity(2));
            return (char) ((read() << 0) + (read() << 8));
        }

        public int readInt() {
            assert (hasCapacity(4));
            return (read() << 0) + (read() << 8) + (read() << 16) + (read() << 24);
        }

        public long readLong() {
            assert (hasCapacity(8));
            return (read() << 0) + (read() << 8) + (read() << 16) + ((long) read() << 24) + ((long) read() << 32) + ((long) read() << 40)
                    + ((long) read() << 48) + ((long) read() << 56);
        }

        public double readDouble() {
            return Double.longBitsToDouble(readLong());
        }

        public float readFloat() {
            return Float.intBitsToFloat(readInt());
        }

        public void writeShort(int v) {
            assert (hasCapacity(2));
            write((v >>> 0) & 0xFF);
            write((v >>> 8) & 0xFF);
        }

        public void writeChar(int v) {
            assert (hasCapacity(2));
            write((v >>> 0) & 0xFF);
            write((v >>> 8) & 0xFF);
        }


        public void writeInt(int v) {
            assert (hasCapacity(4));
            write((v >>> 0) & 0xFF);
            write((v >>> 8) & 0xFF);
            write((v >>> 16) & 0xFF);
            write((v >>> 24) & 0xFF);
        }


        public void writeLong(long v) {
            assert (hasCapacity(8));
            write((int) (v >>> 0) & 0xFF);
            write((int) (v >>> 8) & 0xFF);
            write((int) (v >>> 16) & 0xFF);
            write((int) (v >>> 24) & 0xFF);
            write((int) (v >>> 32) & 0xFF);
            write((int) (v >>> 40) & 0xFF);
            write((int) (v >>> 48) & 0xFF);
            write((int) (v >>> 56) & 0xFF);
        }

        public void writeDouble(double v) {
            writeLong(Double.doubleToLongBits(v));
        }


        public void writeFloat(float v) {
            writeInt(Float.floatToIntBits(v));
        }

        public void writeRawDouble(double v) {
            writeLong(Double.doubleToRawLongBits(v));
        }


        public void writeRawFloat(float v) {
            writeInt(Float.floatToRawIntBits(v));
        }

    }

    ;
}
