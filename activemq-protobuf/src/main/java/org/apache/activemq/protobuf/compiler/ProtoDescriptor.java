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

public class ProtoDescriptor {

    private String packageName;
    private Map<String, OptionDescriptor> options;
    private Map<String, MessageDescriptor> messages;
    private Map<String, EnumDescriptor> enums;
    private List<ExtendDescriptor> extendsList;
    private Map<String, ServiceDescriptor> services;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setOptions(Map<String,OptionDescriptor> options) {
        this.options = options;
    }

    public void setMessages(Map<String,MessageDescriptor> messages) {
        this.messages = messages;
    }

    public void setEnums(Map<String,EnumDescriptor> enums) {
        this.enums = enums;
    }

    public void setExtends(List<ExtendDescriptor> extendsList) {
        this.extendsList = extendsList;
    }

    public List<ExtendDescriptor> getExtends() {
        return extendsList;
    }

    public String getPackageName() {
        return packageName;
    }

    public Map<String, OptionDescriptor> getOptions() {
        return options;
    }

    public Map<String,MessageDescriptor> getMessages() {
        return messages;
    }

    public Map<String,EnumDescriptor> getEnums() {
        return enums;
    }

    public void setServices(Map<String,ServiceDescriptor> services) {
        this.services = services;
    }

    public Map<String,ServiceDescriptor> getServices() {
        return services;
    }

    /**
     * Checks for validation errors in the proto definition and fills them 
     * into the errors list.
     * 
     * @return
     */
    public void validate(List<String> errors) {
        for (OptionDescriptor o : options.values()) {
            o.validate(errors);
        }
        for (MessageDescriptor o : messages.values()) {
            o.validate(errors);
        }
        for (EnumDescriptor o : enums.values()) {
            o.validate(errors);
        }
        for (ExtendDescriptor o : extendsList) {
            o.validate(errors);
        }
        for (ServiceDescriptor o : services.values()) {
            o.validate(errors);
        }
    }

}
