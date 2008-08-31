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

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistry;

import static org.apache.activemq.protobuf.WireInfo.*;

import java.io.IOException;

abstract public class Message<T> {

    protected int memoizedSerializedSize = -1;

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

    static protected <T extends Message> T readMessage(CodedInputStream input, ExtensionRegistry extensionRegistry,  T message) throws IOException {
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
        return CodedOutputStream.computeTagSize(tag) +
               CodedOutputStream.computeRawVarint32Size(t)+t;
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

    abstract public boolean isInitialized();
}
