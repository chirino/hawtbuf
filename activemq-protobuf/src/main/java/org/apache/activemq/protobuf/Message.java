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

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

import static org.apache.activemq.protobuf.WireInfo.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

abstract public class Message<T> {

    protected int memoizedSerializedSize = -1;

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

    static protected void writeGroup(CodedOutputStream output, int tag, Message message) throws IOException {
        output.writeTag(tag, WIRETYPE_START_GROUP);
        message.writeTo(output);
        output.writeTag(tag, WIRETYPE_END_GROUP);
    }

    static protected void writeMessage(CodedOutputStream output, int tag, Message message) throws IOException {
        output.writeTag(tag, WIRETYPE_LENGTH_DELIMITED);
        output.writeRawVarint32(message.serializedSize());
        message.writeTo(output);
    }

    static protected <T extends Message> T readGroup(CodedInputStream input, ExtensionRegistry extensionRegistry, int tag, T group) throws IOException {
        group.mergeFrom(input, extensionRegistry);
        input.checkLastTagWas(makeTag(tag, WIRETYPE_END_GROUP));
        return group;
    }

    static protected <T extends Message> T readMessage(CodedInputStream input, ExtensionRegistry extensionRegistry, T message) throws IOException {
        int length = input.readRawVarint32();
        int oldLimit = input.pushLimit(length);
        message.mergeFrom(input, extensionRegistry);
        input.checkLastTagWas(0);
        input.popLimit(oldLimit);
        return message;
    }

    static protected int computeGroupSize(int tag, Message message) {
        return CodedOutputStream.computeTagSize(tag) * 2 + message.serializedSize();
    }

    static protected int computeMessageSize(int tag, Message message) {
        int t = message.serializedSize();
        return CodedOutputStream.computeTagSize(tag) + CodedOutputStream.computeRawVarint32Size(t) + t;
    }

    abstract public T mergeFrom(T other);

    public T mergeFrom(CodedInputStream input) throws IOException {
        return mergeFrom(input, ExtensionRegistry.getEmptyRegistry());
    }

    abstract public T mergeFrom(CodedInputStream input, ExtensionRegistry extensionRegistry) throws IOException;

    abstract public void writeTo(CodedOutputStream output) throws java.io.IOException;

    abstract public T clone();

    abstract public int serializedSize();

    abstract public void clear();

    abstract public T assertInitialized() throws com.google.protobuf.UninitializedMessageException;
    
    public byte[] toByteArray() {
        try {
            byte[] result = new byte[serializedSize()];
            CodedOutputStream output = CodedOutputStream.newInstance(result);
            writeTo(output);
            output.checkNoSpaceLeft();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " + "(should never happen).", e);
        }
    }

    protected List<String> prefix(List<String> missingFields, String prefix) {
        ArrayList<String> rc = new ArrayList<String>(missingFields.size());
        for (String v : missingFields) {
            rc.add(prefix+v);
        }
        return rc;
    }

    public void writeTo(OutputStream output) throws IOException {
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        writeTo(codedOutput);
        codedOutput.flush();
    }

    public T mergeFrom(ByteString data) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = data.newCodedInput();
            mergeFrom(input);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a ByteString threw an IOException (should " + "never happen).", e);
        }
    }

    public T mergeFrom(ByteString data, ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = data.newCodedInput();
            mergeFrom(input, extensionRegistry);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a ByteString threw an IOException (should " + "never happen).", e);
        }
    }

    public T mergeFrom(byte[] data) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = CodedInputStream.newInstance(data);
            mergeFrom(input);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should " + "never happen).", e);
        }
    }

    public T mergeFrom(byte[] data, ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException {
        try {
            CodedInputStream input = CodedInputStream.newInstance(data);
            mergeFrom(input, extensionRegistry);
            input.checkLastTagWas(0);
            return (T)this;
        } catch (InvalidProtocolBufferException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should " + "never happen).", e);
        }
    }

    public T mergeFrom(InputStream input) throws IOException {
        CodedInputStream codedInput = CodedInputStream.newInstance(input);
        mergeFrom(codedInput);
        codedInput.checkLastTagWas(0);
        return (T)this;
    }

    public T mergeFrom(InputStream input, ExtensionRegistry extensionRegistry) throws IOException {
        CodedInputStream codedInput = CodedInputStream.newInstance(input);
        mergeFrom(codedInput, extensionRegistry);
        codedInput.checkLastTagWas(0);
        return (T)this;
    }

}
