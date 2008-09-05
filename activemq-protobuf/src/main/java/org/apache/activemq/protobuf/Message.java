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

public interface Message<T> {

    public T mergeFrom(T other);

    public T mergeFrom(CodedInputStream input) throws IOException;

    public T mergeFrom(CodedInputStream input, ExtensionRegistry extensionRegistry) throws IOException;

    public void writeTo(CodedOutputStream output) throws java.io.IOException;

    public T clone() throws CloneNotSupportedException;

    public int serializedSize();

    public void clear();

    public T assertInitialized() throws com.google.protobuf.UninitializedMessageException;

    public byte[] toByteArray();

    public void writePartialTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException;
    
    public void writeTo(OutputStream output) throws IOException;

    public T mergeFrom(ByteString data) throws InvalidProtocolBufferException;

    public T mergeFrom(ByteString data, ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException;

    public T mergeFrom(byte[] data) throws InvalidProtocolBufferException;

    public T mergeFrom(byte[] data, ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException;

    public T mergeFrom(InputStream input) throws IOException;

    public T mergeFrom(InputStream input, ExtensionRegistry extensionRegistry) throws IOException;

}
