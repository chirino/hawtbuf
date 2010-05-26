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

/**
 * Used to write and read primitives to and from a Buffer.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class BufferEditor {

    private BufferEditor() {
    }

    public byte[] toByteArray(Buffer buffer) {
        if (buffer.offset == 0 && buffer.length == buffer.data.length) {
            return buffer.data;
        }

        byte rc[] = new byte[buffer.length];
        System.arraycopy(buffer.data, buffer.offset, rc, 0, buffer.length);
        return rc;
    }

    protected void spaceNeeded(Buffer buffer, int i) {
        assert buffer.offset + i <= buffer.length;
    }

    public int remaining(Buffer buffer) {
        return buffer.length - buffer.offset;
    }

    public int read(Buffer buffer) {
        return buffer.data[buffer.offset++] & 0xff;
    }

    public void readFully(Buffer buffer, byte[] b) throws IOException {
        readFully(buffer, b, 0, b.length);
    }

    public void readFully(Buffer buffer, byte[] b, int off, int len) throws IOException {
        spaceNeeded(buffer, len);
        System.arraycopy(buffer.data, buffer.offset, b, off, len);
        buffer.offset += len;
    }

    public int skipBytes(Buffer buffer, int n) throws IOException {
        int rc = Math.min(n, remaining(buffer));
        buffer.offset += rc;
        return rc;
    }

    public boolean readBoolean(Buffer buffer) throws IOException {
        spaceNeeded(buffer, 1);
        return read(buffer) != 0;
    }

    public byte readByte(Buffer buffer) throws IOException {
        spaceNeeded(buffer, 1);
        return (byte) read(buffer);
    }

    public int readUnsignedByte(Buffer buffer) throws IOException {
        spaceNeeded(buffer, 1);
        return read(buffer);
    }

    public void write(Buffer buffer, int b) throws IOException {
        spaceNeeded(buffer, 1);
        buffer.data[buffer.offset++] = (byte) b;
    }

    public void write(Buffer buffer, byte[] b) throws IOException {
        write(buffer, b, 0, b.length);
    }

    public void write(Buffer buffer, byte[] b, int off, int len) throws IOException {
        spaceNeeded(buffer, len);
        System.arraycopy(b, off, buffer.data, buffer.offset, len);
        buffer.offset += len;
    }

    public void writeBoolean(Buffer buffer, boolean v) throws IOException {
        spaceNeeded(buffer, 1);
        write(buffer, v ? 1 : 0);
    }

    public void writeByte(Buffer buffer, int v) throws IOException {
        spaceNeeded(buffer, 1);
        write(buffer, v);
    }


    abstract public short readShort(Buffer buffer) throws IOException;

    abstract public int readUnsignedShort(Buffer buffer) throws IOException;

    abstract public char readChar(Buffer buffer) throws IOException;

    abstract public int readInt(Buffer buffer) throws IOException;

    abstract public long readLong(Buffer buffer) throws IOException;

    abstract public double readDouble(Buffer buffer) throws IOException;

    abstract public float readFloat(Buffer buffer) throws IOException;

    abstract public void writeShort(Buffer buffer, int v) throws IOException;

    abstract public void writeChar(Buffer buffer, int v) throws IOException;

    abstract public void writeInt(Buffer buffer, int v) throws IOException;

    abstract public void writeLong(Buffer buffer, long v) throws IOException;

    abstract public void writeDouble(Buffer buffer, double v) throws IOException;

    abstract public void writeFloat(Buffer buffer, float v) throws IOException;

    abstract public void writeRawDouble(Buffer buffer, double v) throws IOException;

    abstract public void writeRawFloat(Buffer buffer, float v) throws IOException;

    public static final BufferEditor BIG_ENDIAN = new BufferEditor() {

        public short readShort(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 2);
            return (short) ((read(buffer) << 8) + (read(buffer) << 0));
        }


        public int readUnsignedShort(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 2);
            return (read(buffer) << 8) + (read(buffer) << 0);
        }

        public char readChar(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 2);
            return (char) ((read(buffer) << 8) + (read(buffer) << 0));
        }


        public int readInt(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 4);
            return (read(buffer) << 24) + (read(buffer) << 16) + (read(buffer) << 8) + (read(buffer) << 0);
        }


        public long readLong(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 8);
            return ((long) read(buffer) << 56) + ((long) read(buffer) << 48) + ((long) read(buffer) << 40) + ((long) read(buffer) << 32) + ((long) read(buffer) << 24)
                    + ((read(buffer)) << 16) + ((read(buffer)) << 8) + ((read(buffer)) << 0);
        }


        public double readDouble(Buffer buffer) throws IOException {
            return Double.longBitsToDouble(readLong(buffer));
        }


        public float readFloat(Buffer buffer) throws IOException {
            return Float.intBitsToFloat(readInt(buffer));
        }

        public void writeShort(Buffer buffer, int v) throws IOException {
            spaceNeeded(buffer, 2);
            write(buffer, (v >>> 8) & 0xFF);
            write(buffer, (v >>> 0) & 0xFF);
        }


        public void writeChar(Buffer buffer, int v) throws IOException {
            spaceNeeded(buffer, 2);
            write(buffer, (v >>> 8) & 0xFF);
            write(buffer, (v >>> 0) & 0xFF);
        }


        public void writeInt(Buffer buffer, int v) throws IOException {
            spaceNeeded(buffer, 4);
            write(buffer, (v >>> 24) & 0xFF);
            write(buffer, (v >>> 16) & 0xFF);
            write(buffer, (v >>> 8) & 0xFF);
            write(buffer, (v >>> 0) & 0xFF);
        }

        public void writeLong(Buffer buffer, long v) throws IOException {
            spaceNeeded(buffer, 8);
            write(buffer, (int) (v >>> 56) & 0xFF);
            write(buffer, (int) (v >>> 48) & 0xFF);
            write(buffer, (int) (v >>> 40) & 0xFF);
            write(buffer, (int) (v >>> 32) & 0xFF);
            write(buffer, (int) (v >>> 24) & 0xFF);
            write(buffer, (int) (v >>> 16) & 0xFF);
            write(buffer, (int) (v >>> 8) & 0xFF);
            write(buffer, (int) (v >>> 0) & 0xFF);
        }


        public void writeDouble(Buffer buffer, double v) throws IOException {
            writeLong(buffer, Double.doubleToLongBits(v));
        }

        public void writeFloat(Buffer buffer, float v) throws IOException {
            writeInt(buffer, Float.floatToIntBits(v));
        }

        public void writeRawDouble(Buffer buffer, double v) throws IOException {
            writeLong(buffer, Double.doubleToRawLongBits(v));
        }

        public void writeRawFloat(Buffer buffer, float v) throws IOException {
            writeInt(buffer, Float.floatToRawIntBits(v));
        }
    };


    public static final BufferEditor LITTLE_ENDIAN = new BufferEditor() {

        public short readShort(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 2);
            return (short) ((read(buffer) << 0) + (read(buffer) << 8));
        }

        public int readUnsignedShort(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 2);
            return (read(buffer) << 0) + (read(buffer) << 8);
        }

        public char readChar(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 2);
            return (char) ((read(buffer) << 0) + (read(buffer) << 8));
        }

        public int readInt(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 4);
            return (read(buffer) << 0) + (read(buffer) << 8) + (read(buffer) << 16) + (read(buffer) << 24);
        }

        public long readLong(Buffer buffer) throws IOException {
            spaceNeeded(buffer, 8);
            return (read(buffer) << 0) + (read(buffer) << 8) + (read(buffer) << 16) + ((long) read(buffer) << 24) + ((long) read(buffer) << 32) + ((long) read(buffer) << 40)
                    + ((long) read(buffer) << 48) + ((long) read(buffer) << 56);
        }

        public double readDouble(Buffer buffer) throws IOException {
            return Double.longBitsToDouble(readLong(buffer));
        }

        public float readFloat(Buffer buffer) throws IOException {
            return Float.intBitsToFloat(readInt(buffer));
        }

        public void writeShort(Buffer buffer, int v) throws IOException {
            spaceNeeded(buffer, 2);
            write(buffer, (v >>> 0) & 0xFF);
            write(buffer, (v >>> 8) & 0xFF);
        }

        public void writeChar(Buffer buffer, int v) throws IOException {
            spaceNeeded(buffer, 2);
            write(buffer, (v >>> 0) & 0xFF);
            write(buffer, (v >>> 8) & 0xFF);
        }


        public void writeInt(Buffer buffer, int v) throws IOException {
            spaceNeeded(buffer, 4);
            write(buffer, (v >>> 0) & 0xFF);
            write(buffer, (v >>> 8) & 0xFF);
            write(buffer, (v >>> 16) & 0xFF);
            write(buffer, (v >>> 24) & 0xFF);
        }


        public void writeLong(Buffer buffer, long v) throws IOException {
            spaceNeeded(buffer, 8);
            write(buffer, (int) (v >>> 0) & 0xFF);
            write(buffer, (int) (v >>> 8) & 0xFF);
            write(buffer, (int) (v >>> 16) & 0xFF);
            write(buffer, (int) (v >>> 24) & 0xFF);
            write(buffer, (int) (v >>> 32) & 0xFF);
            write(buffer, (int) (v >>> 40) & 0xFF);
            write(buffer, (int) (v >>> 48) & 0xFF);
            write(buffer, (int) (v >>> 56) & 0xFF);
        }

        public void writeDouble(Buffer buffer, double v) throws IOException {
            writeLong(buffer, Double.doubleToLongBits(v));
        }


        public void writeFloat(Buffer buffer, float v) throws IOException {
            writeInt(buffer, Float.floatToIntBits(v));
        }

        public void writeRawDouble(Buffer buffer, double v) throws IOException {
            writeLong(buffer, Double.doubleToRawLongBits(v));
        }


        public void writeRawFloat(Buffer buffer, float v) throws IOException {
            writeInt(buffer, Float.floatToRawIntBits(v));
        }

    };
}
