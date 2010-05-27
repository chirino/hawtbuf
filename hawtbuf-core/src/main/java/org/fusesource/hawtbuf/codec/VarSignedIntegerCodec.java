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
 * Implementation of a variable length Codec for a signed Integer
 *
 */
public class VarSignedIntegerCodec extends VarIntegerCodec {

    public static final VarSignedIntegerCodec INSTANCE = new VarSignedIntegerCodec();


    public void encode(Integer value, DataOutput dataOut) throws IOException {
        super.encode(encodeZigZag(value), dataOut);
    }

    public Integer decode(DataInput dataIn) throws IOException {
        return decodeZigZag(super.decode(dataIn));
    }

    private static int decodeZigZag(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private static int encodeZigZag(int n) {
        return (n << 1) ^ (n >> 31);
    }

    public int estimatedSize(Integer value) {
        return super.estimatedSize(encodeZigZag(value));
    }
}