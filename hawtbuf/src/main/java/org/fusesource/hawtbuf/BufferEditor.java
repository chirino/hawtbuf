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
abstract public class BufferEditor extends AbstractVarIntSupport {
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


    static public BufferEditor big(Buffer buffer) {
        return new BigEndianBufferEditor(buffer);
    }
    
    static public BufferEditor little(Buffer buffer) {
        return new LittleEndianBufferEditor(buffer);
    }

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
