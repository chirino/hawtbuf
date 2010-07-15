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
package org.fusesource.hawtbuf.codec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.ProtocolException;

/**
 * Implementation of a variable length Codec for an Integer
 *
 */
public class VarIntegerCodec implements Codec<Integer> {

    public static final VarIntegerCodec INSTANCE = new VarIntegerCodec();

    public void encode(Integer x, DataOutput dataOut) throws IOException {
        int value = x;
        while (true) {
            if ((value & ~0x7F) == 0) {
                dataOut.writeByte(value);
                return;
            } else {
                dataOut.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public Integer decode(DataInput dataIn) throws IOException {
        byte tmp = dataIn.readByte();
        if (tmp >= 0) {
            return (int)tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = dataIn.readByte()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = dataIn.readByte()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = dataIn.readByte()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = dataIn.readByte()) << 28;
                    if (tmp < 0) {
                        // Discard upper 32 bits.
                        for (int i = 0; i < 5; i++) {
                            if (dataIn.readByte() >= 0)
                                return result;
                        }
                        throw new ProtocolException("Encountered a malformed variable int");
                    }
                }
            }
        }
        return result;
    }

    public int getFixedSize() {
        return -1;
    }

    /**
     * @return the source object since integers are immutable.
     */
    public Integer deepCopy(Integer source) {
        return source;
    }

    public boolean isDeepCopySupported() {
        return true;
    }

    public boolean isEstimatedSizeSupported() {
        return true;
    }

    public int estimatedSize(Integer x) {
        int value = x;
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
}