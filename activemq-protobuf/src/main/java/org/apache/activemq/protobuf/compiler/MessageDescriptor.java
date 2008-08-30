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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MessageDescriptor implements TypeDescriptor {

    private String name;
    private ExtensionsDescriptor extensions;
    private Map<String,FieldDescriptor> fields = new LinkedHashMap<String, FieldDescriptor>();
    private Map<String,MessageDescriptor> messages = new LinkedHashMap<String,MessageDescriptor>();
    private Map<String,EnumDescriptor> enums = new LinkedHashMap<String, EnumDescriptor>();
    private final ProtoDescriptor protoDescriptor;
    private List<MessageDescriptor> extendsList = new ArrayList<MessageDescriptor>();
    private Map<String, OptionDescriptor> options = new LinkedHashMap<String, OptionDescriptor>();
    private final MessageDescriptor parent;

    public MessageDescriptor(ProtoDescriptor protoDescriptor, MessageDescriptor parent) {
        this.protoDescriptor = protoDescriptor;
        this.parent = parent;
    }
    
    public void validate(List<String> errors) {
        for (FieldDescriptor field : fields.values()) {
            field.validate(errors);
        }
        for (EnumDescriptor o : enums.values()) {
            o.validate(errors);
        }
        for (MessageDescriptor o : messages.values()) {
            o.validate(errors);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtensions(ExtensionsDescriptor extensions) {
        this.extensions = extensions;
    }

    public void setExtends(List<MessageDescriptor> extendsList) {
        this.extendsList = extendsList;
    }
    public List<MessageDescriptor> getExtends() {
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

    public String getQName() {
        if( parent==null ) {
            return name;
        } else {
            return parent.getQName()+"."+name;
        }
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

    public Map<String, OptionDescriptor> getOptions() {
        return options;
    }

    public void setOptions(Map<String, OptionDescriptor> options) {
        this.options = options;
    }

    public MessageDescriptor getParent() {
        return parent;
    }

    public TypeDescriptor getType(String t) {
        for (MessageDescriptor o : messages.values()) {
            if( t.equals(o.getName()) ) {
                return o;
            }
            if( t.startsWith(o.getName()+".") ) {
                return o.getType( t.substring(o.getName().length()+1) );
            }
        }
        for (EnumDescriptor o : enums.values()) {
            if( t.equals(o.getName()) ) {
                return o;
            }
        }
        return null;
    }

}
