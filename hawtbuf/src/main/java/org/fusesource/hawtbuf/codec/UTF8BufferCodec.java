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

import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.UTF8Buffer;

/**
 * Implementation of a Codec for UTF8Buffer objects.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 *
 */
public class UTF8BufferCodec extends AbstractBufferCodec<UTF8Buffer> {
    public static final UTF8BufferCodec INSTANCE = new UTF8BufferCodec();

    @Override
    protected UTF8Buffer createBuffer(byte[] data) {
        return new UTF8Buffer(data);
    }
    
}
