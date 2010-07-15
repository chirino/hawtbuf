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
 */
public interface Codec<T> {
    
    /**
     * Write the payload of the object to the DataOutput stream.
     * 
     * @param object 
     * @param dataOut
     * @throws IOException
     */
    void encode(T object, DataOutput dataOut) throws IOException;
    
    
    /**
     * Read the payload of the object from the DataInput stream.
     * 
     * @param dataIn 
     * @return unmarshalled object
     * @throws IOException
     */
    T decode(DataInput dataIn) throws IOException;

    /** 
     * @return -1 if the object do not always marshall to a fixed size, otherwise return that fixed size.
     */
    int getFixedSize();
    
    /**
     *
     * @return true if the {@link #estimatedSize(Object)} operation is supported.
     */
    boolean isEstimatedSizeSupported();

    /**
     * @param object
     * @return the estimated marshaled size of the object.
     */
    int estimatedSize(T object);
    
    /**
     * 
     * @return true if the {@link #deepCopy(Object)} operations is supported.
     */
    boolean isDeepCopySupported();

    /**
     * @return a deep copy of the source object.  If the source is immutable
     * the same source should be returned.
     */
    T deepCopy(T source);
   
}
