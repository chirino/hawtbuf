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

package org.apache.activemq.protobuf;

import java.io.UnsupportedEncodingException;

final public class Buffer {

    public byte[] data;
    public int offset;
    public int length;

    public Buffer() {
    }
    
    public Buffer(String input) {
        this( encode(input) );
    }
    
    public Buffer(byte data[]) {
        this.data = data;
        this.offset = 0;
        this.length = data.length;
    }

    public Buffer(byte data[], int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }


    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void compact() {
        if (length != data.length) {
            byte t[] = new byte[length];
            System.arraycopy(data, offset, t, 0, length);
            data = t;
            offset = 0;
        }
    }
    
    public byte[] toByteArray() {
        if (length != data.length) {
            byte t[] = new byte[length];
            System.arraycopy(data, offset, t, 0, length);
            data = t;
            offset = 0;
        }
        return data;
    }

    public String toStringUtf8() {
        try {
            return new String(data, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("A UnsupportedEncodingException was thrown for teh UTF-8 encoding. (This should never happen)");
        }
    }

    public byte byteAt(int i) {
        return data[offset+i];
    }
    
    private static byte[] encode(String input) {
        try {
            return input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("A UnsupportedEncodingException was thrown for teh UTF-8 encoding. (This should never happen)");
        }
    }

    @Override
    public int hashCode() {
        byte []target = new byte[4];
        for(int i=0; i < length; i++) {
            target[i%4] ^= data[offset+i];
        }
        return target[0]<<24 | target[1]<<16 | target[2]<<8 | target[3];
    }
    
    @Override
    public boolean equals(Object obj) {
        if( obj==this )
           return true;
        
        if( obj==null || obj.getClass()!=Buffer.class )
           return false;
        
        return equals((Buffer)obj);
     }
     
     public boolean equals(Buffer obj) {
        if( length != obj.length ) {
            return false;
        }
        for(int i=0; i < length; i++) {
           if( obj.data[obj.offset+i] != data[offset+i] ) {
               return false;
           }
        }
        return true;
     }

     public BufferInputStream newInput() {
         return new BufferInputStream(this);
     }
     
     public BufferOutputStream newOutput() {
         return new BufferOutputStream(this);
     }
}
