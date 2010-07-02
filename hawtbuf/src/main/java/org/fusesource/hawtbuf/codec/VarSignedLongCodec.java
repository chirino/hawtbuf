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

/**
 * Implementation of a variable length Codec for a signed Long
 *
 */
public class VarSignedLongCodec extends VarLongCodec {

    public static final VarSignedLongCodec INSTANCE = new VarSignedLongCodec();


    public void encode(Long value, DataOutput dataOut) throws IOException {
        super.encode(encodeZigZag(value), dataOut);
    }

    public Long decode(DataInput dataIn) throws IOException {
        return decodeZigZag(super.decode(dataIn));
    }

    private static long decodeZigZag(long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private static long encodeZigZag(long n) {
        return (n << 1) ^ (n >> 63);
    }

    public int estimatedSize(Long value) {
        return super.estimatedSize(encodeZigZag(value));
    }
}