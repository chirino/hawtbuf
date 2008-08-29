package org.apache.activemq.protobuf.compiler;

import java.util.Map;

public class GroupDescriptor extends FieldDescriptor {
    private Map<String,FieldDescriptor> fields;

    public GroupDescriptor(ProtoDescriptor protoDescriptor) {
        super(protoDescriptor);
    }

    public Map<String, FieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldDescriptor> fields) {
        this.fields = fields;
    }

}
