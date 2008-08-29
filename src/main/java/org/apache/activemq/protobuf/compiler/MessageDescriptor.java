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
package org.apache.activemq.protobuf.compiler;

import java.util.List;
import java.util.Map;


public class MessageDescriptor {

    private String name;
    private ExtensionsDescriptor extensions;
    private Map<String,FieldDescriptor> fields;
    private Map<String,MessageDescriptor> messages;
    private Map<String,EnumDescriptor> enums;
    private final ProtoDescriptor protoDescriptor;
    private List<ExtendDescriptor> extendsList;

    public MessageDescriptor(ProtoDescriptor protoDescriptor) {
        this.protoDescriptor = protoDescriptor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtensions(ExtensionsDescriptor extensions) {
        this.extensions = extensions;
    }

    public void setExtends(List<ExtendDescriptor> extendsList) {
        this.extendsList = extendsList;
    }
    public List<ExtendDescriptor> getExtends() {
        return extendsList;
    }

    public void setFields(Map<String,FieldDescriptor> fields) {
        this.fields = fields;
    }

    public void setMessages(Map<String,MessageDescriptor> messages) {
        this.messages = messages;
    }

    public void setEnums(Map<String,EnumDescriptor> enums) {
        this.enums = enums;
    }

    public String getName() {
        return name;
    }

    public ExtensionsDescriptor getExtensions() {
        return extensions;
    }

    public Map<String,FieldDescriptor> getFields() {
        return fields;
    }

    public Map<String,MessageDescriptor> getMessages() {
        return messages;
    }

    public Map<String,EnumDescriptor> getEnums() {
        return enums;
    }

    public ProtoDescriptor getProtoDescriptor() {
        return protoDescriptor;
    }

    public void validate(List<String> errors) {
        // TODO Auto-generated method stub
        
    }

}
