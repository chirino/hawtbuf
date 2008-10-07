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

import static org.apache.activemq.protobuf.WireInfo.WIRETYPE_END_GROUP;
import static org.apache.activemq.protobuf.WireInfo.WIRETYPE_LENGTH_DELIMITED;
import static org.apache.activemq.protobuf.WireInfo.WIRETYPE_START_GROUP;
import static org.apache.activemq.protobuf.WireInfo.makeTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

abstract public class BaseMessage<T> implements Message<T> {

    protected int memoizedSerializedSize = -1;
    
    abstract public T clone() throws CloneNotSupportedException;

    ///////////////////////////////////////////////////////////////////
    // Write related helpers.
    ///////////////////////////////////////////////////////////////////

    public void writeFramed(CodedOutputStream output) throws IOException {
        output.writeRawVarint32(serializedSizeUnframed());
        writeUnframed(output);
    }

    public byte[] toUnframedByteArray() {
        try {
            byte[] result = new byte[serializedSizeUnframed()];
            CodedOutputStream output = CodedOutputStream.newInstance(result);
            writeUnframed(output);
            output.checkNoSpaceLeft();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " + "(should never happen).", e);
        }
    }
    
    
	public byte[] toFramedByteArray() {
        try {
            byte[] result = new byte[serializedSizeFramed()];
            CodedOutputStream output = CodedOutputStream.newInstance(result);
            writeFramed(output);
            output.checkNoSpaceLeft();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " + "(should never happen).", e);
        }
	}
    
	public void writeFramed(OutputStream output) throws IOException {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        writeFramed(codedOutput);
        codedOutput.flush();
	}

    public void writeUnframed(OutputStream output) throws IOException {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        writeUnframed(codedOutput);
        codedOutput.flush();
    }
    
    public int serializedSizeFramed() {
        int t = serializedSizeUnframed();
        return CodedOutputStream.computeRawVarint32Size(t) + t;

    }

    ///////////////////////////////////////////////////////////////////
    // Read related helpers.
    ///////////////////////////////////////////////////////////////////

    public T mergeFramed(CodedInputStream input) throws IOException {
        int length = input.readRawVarint32();
        int oldLimit = input.pushLimit(length);
        T rc=  mergeUnframed(input);
        input.checkLastTagWas(0);
        input.popLimit(oldLimit);
        return rc;
    }

    public T mergeUnframed(ByteString data) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = data.newCodedInput();
            mergeUnframed(input);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a ByteString threw an IOException (should " + "never happen).", e);
        }
    }
    
    public T mergeFramed(ByteString data) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = data.newCodedInput();
            mergeFramed(input);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a ByteString threw an IOException (should " + "never happen).", e);
        }
    }

    public T mergeUnframed(byte[] data) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = CodedInputStream.newInstance(data);
            mergeUnframed(input);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should " + "never happen).", e);
        }
    }
    
    public T mergeFramed(byte[] data) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = CodedInputStream.newInstance(data);
            mergeFramed(input);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should " + "never happen).", e);
        }
    }

    public T mergeUnframed(InputStream input) throws IOException {
        CodedInputStream codedInput = CodedInputStream.newInstance(input);
        mergeUnframed(codedInput);
        return (T)this;
    }
    
    public T mergeFramed(InputStream input) throws IOException {
		int length = readRawVarint32(input);
		byte []data = new byte[length];
		int pos = 0;
		while( pos < length ) {
			int r = input.read(data, pos, length-pos);
			if( r < 0 ) {
				throw new InvalidProtocolBufferException("Input stream ended before a full message frame could be read.");	
			}
			pos+=r;
		}
		return mergeUnframed(data);
    }

    ///////////////////////////////////////////////////////////////////
    // Internal implementation methods.
    ///////////////////////////////////////////////////////////////////
    static protected <T> void addAll(Iterable<T> values, Collection<? super T> list) {
        if (values instanceof Collection) {
            @SuppressWarnings("unsafe")
            Collection<T> collection = (Collection<T>)values;
            list.addAll(collection);
        } else {
            for (T value : values) {
                list.add(value);
            }
        }
    }
    
    static protected void writeGroup(CodedOutputStream output, int tag, BaseMessage message) throws IOException {
        output.writeTag(tag, WIRETYPE_START_GROUP);
        message.writeUnframed(output);
        output.writeTag(tag, WIRETYPE_END_GROUP);
    }

    static protected <T extends BaseMessage> T readGroup(CodedInputStream input, int tag, T group) throws IOException {
        group.mergeUnframed(input);
        input.checkLastTagWas(makeTag(tag, WIRETYPE_END_GROUP));
        return group;
    }

    static protected int computeGroupSize(int tag, BaseMessage message) {
        return CodedOutputStream.computeTagSize(tag) * 2 + message.serializedSizeUnframed();
    }


    static protected void writeMessage(CodedOutputStream output, int tag, BaseMessage message) throws IOException {
        output.writeTag(tag, WIRETYPE_LENGTH_DELIMITED);
        message.writeFramed(output);
    }
    
    static protected int computeMessageSize(int tag, BaseMessage message) {
        return CodedOutputStream.computeTagSize(tag) + message.serializedSizeFramed();
    }
    
    protected List<String> prefix(List<String> missingFields, String prefix) {
        ArrayList<String> rc = new ArrayList<String>(missingFields.size());
        for (String v : missingFields) {
            rc.add(prefix+v);
        }
        return rc;
    }


    /**
     * Read a raw Varint from the stream.  If larger than 32 bits, discard the
     * upper bits.
     */
    static protected int readRawVarint32(InputStream is) throws IOException {
      byte tmp = readRawByte(is);
      if (tmp >= 0) {
        return tmp;
      }
      int result = tmp & 0x7f;
      if ((tmp = readRawByte(is)) >= 0) {
        result |= tmp << 7;
      } else {
        result |= (tmp & 0x7f) << 7;
        if ((tmp = readRawByte(is)) >= 0) {
          result |= tmp << 14;
        } else {
          result |= (tmp & 0x7f) << 14;
          if ((tmp = readRawByte(is)) >= 0) {
            result |= tmp << 21;
          } else {
            result |= (tmp & 0x7f) << 21;
            result |= (tmp = readRawByte(is)) << 28;
            if (tmp < 0) {
              // Discard upper 32 bits.
              for (int i = 0; i < 5; i++) {
                if (readRawByte(is) >= 0) return result;
              }
              throw new InvalidProtocolBufferException(
              "CodedInputStream encountered a malformed varint.");
            }
          }
        }
      }
      return result;
    }

    static protected byte readRawByte(InputStream is) throws IOException {
    	int rc = is.read();
    	if( rc == -1 ) {
	        throw new InvalidProtocolBufferException(
	        	      "While parsing a protocol message, the input ended unexpectedly " +
	        	      "in the middle of a field.  This could mean either than the " +
	        	      "input has been truncated or that an embedded message " +
	        	      "misreported its own length.");
    	}
    	return (byte) rc;
    }
}
