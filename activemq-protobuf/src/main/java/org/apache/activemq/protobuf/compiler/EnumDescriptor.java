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

public class EnumDescriptor {

    private String name;
    private Map<String,EnumFieldDescriptor> fields;
    private final ProtoDescriptor protoDescriptor;

    public EnumDescriptor(ProtoDescriptor protoDescriptor) {
        this.protoDescriptor = protoDescriptor;
    }

    public String getName() {
        return name;
    }

    public Map<String,EnumFieldDescriptor> getFields() {
        return fields;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFields(Map<String,EnumFieldDescriptor> fields) {
        this.fields = fields;
    }
    public ProtoDescriptor getProtoDescriptor() {
        return protoDescriptor;
    }

    public void validate(List<String> errors) {
        // TODO Auto-generated method stub
        
    }


}
