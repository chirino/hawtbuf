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

import com.google.protobuf.Message;
import com.google.protobuf.WireFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.activemq.protobuf.compiler.parser.ParseException;
import org.apache.activemq.protobuf.compiler.parser.ProtoParser;

import static org.apache.activemq.protobuf.WireInfo.*;

public class JavaGenerator {

    private File out = new File(".");
    private File[] path = new File[]{new File(".")};

    private ProtoDescriptor proto;
    private String javaPackage;
    private String outerClassName;
    private File outputFile;
    private PrintWriter w;
    private int indent;
    private String optimizeFor;
    private ArrayList<String> errors = new ArrayList<String>();

    public static void main(String[] args) {
        
        JavaGenerator generator = new JavaGenerator();
        args = CommandLineSupport.setOptions(generator, args);
        
        if (args.length == 0) {
            System.out.println("No proto files specified.");
        }
        for (int i = 0; i < args.length; i++) {
            try {
                System.out.println("Compiling: "+args[i]);
                generator.compile(new File(args[i]));
            } catch (CompilerException e) {
                System.out.println("Protocol Buffer Compiler failed with the following error(s):");
                for (String error : e.getErrors() ) {
                    System.out.println("");
                    System.out.println(error);
                }
                System.out.println("");
                System.out.println("Compile failed.  For more details see error messages listed above.");
                return;
            }
        }

    }

    static public class CompilerException extends Exception {
        private final List<String> errors;

        public CompilerException(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    public void compile(File file) throws CompilerException {

        // Parse the proto file
        FileInputStream is=null;
        try {
            is = new FileInputStream(file);
            ProtoParser parser = new ProtoParser(is);
            proto = parser.ProtoDescriptor();
            proto.setName(file.getName());
            loadImports(proto, file.getParentFile());
            proto.validate(errors);
        } catch (FileNotFoundException e) {
            errors.add("Failed to open: "+file.getPath()+":"+e.getMessage());
        } catch (ParseException e) {
            errors.add("Failed to parse: "+file.getPath()+":"+e.getMessage());
        } finally {
            try { is.close(); } catch (Throwable ignore){}
        }

        // This would be too fatal to continue..
        if (proto==null) {
            throw new CompilerException(errors);
        }

        // Load the options..
        javaPackage = javaPackage(proto);
        outerClassName = javaClassName(proto);
        optimizeFor = getOption(proto, "optimize_for", "SPEED");

        // Figure out the java file name..
        outputFile = out;
        if (javaPackage != null) {
            String packagePath = javaPackage.replace('.', '/');
            outputFile = new File(outputFile, packagePath);
        }
        outputFile = new File(outputFile, outerClassName + ".java");

        
        if (!errors.isEmpty()) {
            throw new CompilerException(errors);
        }
        // Start writing the output file..
        outputFile.getParentFile().mkdirs();
        
        FileOutputStream fos=null;
        try {
            fos = new FileOutputStream(outputFile);
            w = new PrintWriter(fos);
            generateProtoFile();
            w.flush();
        } catch (FileNotFoundException e) {
            errors.add("Failed to write to: "+outputFile.getPath()+":"+e.getMessage());
        } finally {
            try { fos.close(); } catch (Throwable ignore){}
        }
        if (!errors.isEmpty()) {
            throw new CompilerException(errors);
        }

    }

    private void loadImports(ProtoDescriptor proto, File protoDir) {
        LinkedHashMap<String,ProtoDescriptor> children = new LinkedHashMap<String,ProtoDescriptor>(); 
        for (String imp : proto.getImports()) {
            File file = new File(protoDir, imp);
            for (int i = 0; i < path.length && !file.exists(); i++) {
                file = new File(path[i], imp);
            } 
            if ( !file.exists() ) {
                errors.add("Cannot load import: "+imp);
            }
            
            FileInputStream is=null;
            try {
                is = new FileInputStream(file);
                ProtoParser parser = new ProtoParser(is);
                ProtoDescriptor child = parser.ProtoDescriptor();
                child.setName(file.getName());
                loadImports(child, file.getParentFile());
                children.put(imp, child);
            } catch (ParseException e) {
                errors.add("Failed to parse: "+file.getPath()+":"+e.getMessage());
            } catch (FileNotFoundException e) {
                errors.add("Failed to open: "+file.getPath()+":"+e.getMessage());
            } finally {
                try { is.close(); } catch (Throwable ignore){}
            }
        }
        proto.setImportProtoDescriptors(children);
    }


    private void generateProtoFile() {
        generateFileHeader();
        if (javaPackage != null) {
            p("package " + javaPackage + ";");
            p("");
        }

        p("public class " + outerClassName + " {");
        indent();

        for (EnumDescriptor enumType : proto.getEnums().values()) {
            generateEnum(enumType);
        }
        for (MessageDescriptor m : proto.getMessages().values()) {
            generateMessageBean(m);
        }

        unindent();
        p("}");
    }

    private void generateFileHeader() {
        p("//");
        p("// Generated by protoc, do not edit by hand.");
        p("//");
    }

    private void generateMessageBean(MessageDescriptor m) {
        
        String className = uCamel(m.getName());
        p();
        p("public static final class " + className + " extends org.apache.activemq.protobuf.Message<" + className + "> {");
        p();

        indent();
        
        for (EnumDescriptor enumType : m.getEnums().values()) {
            generateEnum(enumType);
        }

        // Generate the Nested Messages.
        for (MessageDescriptor subMessage : m.getMessages().values()) {
            generateMessageBean(subMessage);
        }

        // Generate the Group Messages
        for (FieldDescriptor field : m.getFields().values()) {
            if( field.isGroup() ) {
                generateMessageBean(field.getGroup());
            }
        }

        // Generate the field accessors..
        for (FieldDescriptor field : m.getFields().values()) {
            generateFieldAccessor(className, field);
        }
        
        generateMethodIsInitialized(m);

        generateMethodClear(m);

        p("public "+className+" clone() {");
        p("   return new "+className+"().mergeFrom(this);");
        p("}");
        p();
        
        generateMethodMergeFromBean(m, className);

        generateMethodSerializedSize(m);
        
        generateMethodMergeFromStream(m, className);

        generateMethodWriteTo(m);

        unindent();
        p("}");
        p();
    }
    
    /**
     * @param m
     */
    private void generateMethodSerializedSize(MessageDescriptor m) {
        p("public int serializedSize() {");
        indent();
        p("if (memoizedSerializedSize != -1)");
        p("   return memoizedSerializedSize;");
        p();
        p("int size = 0;");
        for (FieldDescriptor field : m.getFields().values()) {
            
            String uname = uCamel(field.getName());
            String getter="get"+uname+"()";            
            String type = javaType(field);
            p("if (has"+uname+"()) {");
            indent();
            
            if( field.getRule() == FieldDescriptor.REPEATED_RULE ) {
                p("for ("+type+" i : get"+uname+"List()) {");
                indent();
                getter = "i";
            }

            if( field.getType()==FieldDescriptor.STRING_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeStringSize("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.BYTES_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeBytesSize("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.BOOL_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeBoolSize("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.DOUBLE_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeDoubleSize("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.FLOAT_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeFloatSize("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.INT32_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeInt32Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.INT64_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeInt64Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SINT32_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeSInt32Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SINT64_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeSInt64Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.UINT32_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeUInt32Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.UINT64_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeUInt64Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.FIXED32_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeFixed32Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.FIXED64_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeFixed64Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SFIXED32_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeSFixed32Size("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SFIXED64_TYPE ) {
                p("size += com.google.protobuf.CodedOutputStream.computeSFixed64Size("+field.getTag()+", "+getter+");");
            } else if( field.getTypeDescriptor().isEnum() ) {
                p("size += com.google.protobuf.CodedOutputStream.computeEnumSize("+field.getTag()+", "+getter+".getNumber());");
            } else if ( field.getGroup()!=null ) {
                p("size += computeGroupSize("+field.getTag()+", "+getter+");");
            } else {
                p("size += computeMessageSize("+field.getTag()+", "+getter+");");
            }
            if( field.getRule() == FieldDescriptor.REPEATED_RULE ) {
                unindent();
                p("}");
            }
            //TODO: finish this up.
            unindent();
            p("}");

        }
        // TODO: handle unknown fields
        // size += getUnknownFields().getSerializedSize();");
        p("memoizedSerializedSize = size;");
        p("return size;");
        unindent();
        p("}");
        p();
    }

    /**
     * @param m
     */
    private void generateMethodWriteTo(MessageDescriptor m) {
        p("public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {");
        indent();
        for (FieldDescriptor field : m.getFields().values()) {
            String uname = uCamel(field.getName());
            String getter="get"+uname+"()";            
            String type = javaType(field);
            p("if (has"+uname+"()) {");
            indent();
            
            if( field.getRule() == FieldDescriptor.REPEATED_RULE ) {
                p("for ("+type+" i : get"+uname+"List()) {");
                indent();
                getter = "i";
            }

            if( field.getType()==FieldDescriptor.STRING_TYPE ) {
                p("output.writeString("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.BYTES_TYPE ) {
                p("output.writeBytes("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.BOOL_TYPE ) {
                p("output.writeBool("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.DOUBLE_TYPE ) {
                p("output.writeDouble("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.FLOAT_TYPE ) {
                p("output.writeFloat("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.INT32_TYPE ) {
                p("output.writeInt32("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.INT64_TYPE ) {
                p("output.writeInt64("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SINT32_TYPE ) {
                p("output.writeSInt32("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SINT64_TYPE ) {
                p("output.writeSInt64("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.UINT32_TYPE ) {
                p("output.writeUInt32("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.UINT64_TYPE ) {
                p("output.writeUInt64("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.FIXED32_TYPE ) {
                p("output.writeFixed32("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.FIXED64_TYPE ) {
                p("output.writeFixed64("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SFIXED32_TYPE ) {
                p("output.writeSFixed32("+field.getTag()+", "+getter+");");
            } else if( field.getType()==FieldDescriptor.SFIXED64_TYPE ) {
                p("output.writeSFixed64("+field.getTag()+", "+getter+");");
            } else if( field.getTypeDescriptor().isEnum() ) {
                p("output.writeEnum("+field.getTag()+", "+getter+".getNumber());");
            } else if ( field.getGroup()!=null ) {
                p("writeGroup(output, "+field.getTag()+", "+getter+");");
            } else {
                p("writeMessage(output, "+field.getTag()+", "+getter+");");
            }
            
            if( field.getRule() == FieldDescriptor.REPEATED_RULE ) {
                unindent();
                p("}");
            }
            
            //TODO: finish this up.
            unindent();
            p("}");
        }
        // TODO: handle unknown fields
        // getUnknownFields().writeTo(output);
        unindent();
        p("}");
        p();        
    }

    /**
     * @param m
     * @param className
     */
    private void generateMethodMergeFromStream(MessageDescriptor m, String className) {
        p("public "+className+" mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistry extensionRegistry) throws java.io.IOException {");
        indent(); {
          //TODO: handle unknown fields
          // UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.unknownFields);
            
          p("while (true) {");
          indent(); {
              p("int tag = input.readTag();");
              p("switch (tag) {");
              p("case 0:");
//              p("   this.setUnknownFields(unknownFields.build());");
              p("   return this;");
              p("default: {");
              
              //TODO: handle unknown field types.
//              p("   if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {");
//              p("       this.setUnknownFields(unknownFields.build());");
//              p("       return this;");
//              p("   }");
              
              p("   break;");
              p("}");
              
              
              for (FieldDescriptor field : m.getFields().values()) {
                  String uname = uCamel(field.getName());
                  String setter = "set"+uname;
                  boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
                  if( repeated ) {
                      setter = "get"+uname+"List().add";
                  }
                  
                  
                  
                  if( field.getType()==FieldDescriptor.STRING_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_LENGTH_DELIMITED)+":");
                      indent();
                      p(setter+"(input.readString());");
                  } else if( field.getType()==FieldDescriptor.BYTES_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_LENGTH_DELIMITED)+":");
                      indent();
                      p(setter+"(input.readBytes());");
                  } else if( field.getType()==FieldDescriptor.BOOL_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readBool());");
                  } else if( field.getType()==FieldDescriptor.DOUBLE_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_FIXED64)+":");
                      indent();
                      p(setter+"(input.readDouble());");
                  } else if( field.getType()==FieldDescriptor.FLOAT_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_FIXED32)+":");
                      indent();
                      p(setter+"(input.readFloat());");
                  } else if( field.getType()==FieldDescriptor.INT32_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readInt32());");
                  } else if( field.getType()==FieldDescriptor.INT64_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readInt64());");
                  } else if( field.getType()==FieldDescriptor.SINT32_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readSInt32());");
                  } else if( field.getType()==FieldDescriptor.SINT64_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readSInt64());");
                  } else if( field.getType()==FieldDescriptor.UINT32_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readUInt32());");
                  } else if( field.getType()==FieldDescriptor.UINT64_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      p(setter+"(input.readUInt64());");
                  } else if( field.getType()==FieldDescriptor.FIXED32_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_FIXED32)+":");
                      indent();
                      p(setter+"(input.readFixed32());");
                  } else if( field.getType()==FieldDescriptor.FIXED64_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_FIXED64)+":");
                      indent();
                      p(setter+"(input.readFixed64());");
                  } else if( field.getType()==FieldDescriptor.SFIXED32_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_FIXED32)+":");
                      indent();
                      p(setter+"(input.readSFixed32());");
                  } else if( field.getType()==FieldDescriptor.SFIXED64_TYPE ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_FIXED64)+":");
                      indent();
                      p(setter+"(input.readSFixed64());");
                  } else if( field.getTypeDescriptor().isEnum() ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_VARINT)+":");
                      indent();
                      String type = javaType(field);
                      p("{");
                      indent();
                      p("int t = input.readEnum();");
                      p(""+type+" value = "+type+".valueOf(t);");
                      p("if( value !=null ) {");
                      indent();
                      p(setter+"(value);");
                      unindent();
                      p("}");
                      // TODO: else store it as an known
                      
                      unindent();
                      p("}");
                      
                  } else if ( field.getGroup()!=null ) {
                      p("case "+makeTag(field.getTag(), WIRETYPE_START_GROUP)+":");
                      indent();
                      String type = javaType(field);
                      if( repeated ) {
                          p(setter+"(readGroup(input, extensionRegistry, "+field.getTag()+", new "+type+"()));");
                      } else {
                          p("if (has"+uname+"()) {");
                          indent();
                          p("readGroup(input, extensionRegistry, "+field.getTag()+", get"+uname+"());");
                          unindent();
                          p("} else {");
                          indent();
                          p(setter+"(readGroup(input, extensionRegistry, "+field.getTag()+",new "+type+"()));");
                          unindent();
                          p("}");
                      }
                      p("");
                  } else {
                      p("case "+makeTag(field.getTag(), WIRETYPE_LENGTH_DELIMITED)+":");
                      indent();
                      String type = javaType(field);
                      if( repeated ) {
                          p(setter+"(readMessage(input, extensionRegistry, new "+type+"()));");
                      } else {
                          p("if (has"+uname+"()) {");
                          indent();
                          p("readMessage(input, extensionRegistry,get"+uname+"());");
                          unindent();
                          p("} else {");
                          indent();
                          p(setter+"(readMessage(input, extensionRegistry, new "+type+"()));");
                          unindent();
                          p("}");
                      }
                  }
                  p("break;");
                  unindent();
              }              
              p("}");
          } unindent();
          p("}"); 
        } unindent();
        p("}");
    }

    /**
     * @param m
     * @param className
     */
    private void generateMethodMergeFromBean(MessageDescriptor m, String className) {
        p("public "+className+" mergeFrom("+className+" other) {");
        indent();
        for (FieldDescriptor field : m.getFields().values()) {
            String uname = uCamel(field.getName());
            p("if (other.has"+uname+"()) {");
            indent();

            if( field.isScalarType() || field.getTypeDescriptor().isEnum() ) {
                if( field.isRepeated() ) {
                    p("get"+uname+"List().addAll(other.get"+uname+"List());");
                } else {
                    p("set"+uname+"(other.get"+uname+"());");
                }
            } else {
                
                String type = javaType(field);
                // It's complex type...
                if( field.isRepeated() ) {
                    p("for("+type+" element: other.get"+uname+"List() ) {");
                    indent();
                        p("get"+uname+"List().add(element.clone());");
                    unindent();
                    p("}");
                } else {
                    p("if (has"+uname+"()) {");
                    indent();
                    p("get"+uname+"().mergeFrom(other.get"+uname+"());");
                    unindent();
                    p("} else {");
                    indent();
                    p("set"+uname+"(other.get"+uname+"().clone());");
                    unindent();
                    p("}");
                }
            }
            unindent();
            p("}");
        }
        p("return this;");
        unindent();
        p("}");
        p();
    }

    /**
     * @param m
     */
    private void generateMethodClear(MessageDescriptor m) {
        p("public final void clear() {");
        indent();
        p("memoizedSerializedSize=-1;");
        for (FieldDescriptor field : m.getFields().values()) {
            String uname = uCamel(field.getName());
            p("clear" + uname + "();");
        }
        unindent();
        p("}");
        p();
    }

    /**
     * @param m
     */
    private void generateMethodIsInitialized(MessageDescriptor m) {
        p("public final boolean isInitialized() {");
        indent();
        for (FieldDescriptor field : m.getFields().values()) {
            String uname = uCamel(field.getName());
            if( field.isRequired() ) {
                p("if(  !has" + uname + "() ) {");
                p("   return false;");
                p("}");
            }
        }
        p("return true;");
        unindent();
        p("}");
        p();
    }

    /**
     * @param field
     * @param className 
     */
    private void generateFieldAccessor(String className, FieldDescriptor field) {
        
        String lname = lCamel(field.getName());
        String uname = uCamel(field.getName());
        String type = field.getRule()==FieldDescriptor.REPEATED_RULE ? javaCollectionType(field):javaType(field);
        String typeDefault = javaTypeDefault(field);
        boolean primitive = isPrimitive(field);
        boolean repeated = field.getRule()==FieldDescriptor.REPEATED_RULE;

        // Create the fields..
        p("// " + field.getRule() + " " + field.getType() + " " + field.getName() + " = " + field.getTag() + ";");
        
        if( repeated ) {
            p("private java.util.List<" + type + "> f_" + lname + ";");
            p();
            
            // Create the field accessors
            p("public boolean has" + uname + "() {");
            indent();
            p("return this.f_" + lname + "!=null && !this.f_" + lname + ".isEmpty();");
            unindent();
            p("}");
            p();

            p("public java.util.List<" + type + "> get" + uname + "List() {");
            indent();
            p("if( this.f_" + lname + " == null ) {");
            indent();
            p("this.f_" + lname + " = new java.util.ArrayList<" + type + ">();");
            unindent();
            p("}");
            p("return this.f_" + lname + ";");
            unindent();
            p("}");
            p();

            p("public "+className+" set" + uname + "List(java.util.List<" + type + "> " + lname + ") {");
            indent();
            p("this.f_" + lname + " = " + lname + ";");
            p("return this;");
            unindent();
            p("}");

            p("public void clear" + uname + "() {");
            indent();
            p("this.f_" + lname + " = null;");
            unindent();
            p("}");

        } else {
            
            p("private " + type + " f_" + lname + "= "+typeDefault+";");
            if (primitive) {
                p("private boolean b_" + lname + ";");
            }
            p();
            
            // Create the field accessors
            p("public boolean has" + uname + "() {");
            indent();
            if (primitive) {
                p("return this.b_" + lname + ";");
            } else {
                p("return this.f_" + lname + "!=null;");
            }
            unindent();
            p("}");
            p();

            p("public " + type + " get" + uname + "() {");
            indent();
            p("return this.f_" + lname + ";");
            unindent();
            p("}");
            p();

            p("public "+className+" set" + uname + "(" + type + " " + lname + ") {");
            indent();
            if (primitive) {
                p("this.b_" + lname + " = true;");
            }
            p("this.f_" + lname + " = " + lname + ";");
            p("return this;");
            unindent();
            p("}");

            p("public void clear" + uname + "() {");
            indent();
            if (primitive) {
                p("this.b_" + lname + " = false;");
            }
            p("this.f_" + lname + " = " + typeDefault + ";");
            unindent();
            p("}");
        }

    }

    private String javaTypeDefault(FieldDescriptor field) {
//        OptionDescriptor defaultOption = field.getOptions().get("default");
        if( field.isNumberType() ) {
            return "0";
        }
        if( field.getType() == FieldDescriptor.BOOL_TYPE ) {
            return "false";
        }
        return "null";
    }
    
    private void generateEnum(EnumDescriptor ed) {
        String uname = uCamel(ed.getName());

        // TODO Auto-generated method stub
        p();
        p("public static enum " +uname + " {");
        indent();
        
        
        p();
        int counter=0;
        for (EnumFieldDescriptor field : ed.getFields().values()) {
            boolean last = counter+1 == ed.getFields().size();
            p(field.getName()+"("+counter+", "+field.getValue()+")"+(last?";":",")); 
            counter++;
        }
        p();
        p("private final int index;");
        p("private final int value;");
        p();
        p("private "+uname+"(int index, int value) {");
        p("   this.index = index;");
        p("   this.value = value;");
        p("}");
        p();
        p("public final int getNumber() {");
        p("   return value;");
        p("}");
        p();
        p("public static "+uname+" valueOf(int value) {");
        p("   switch (value) {");
        
        // It's possible to define multiple ENUM fields with the same value.. 
        //   we only want to put the first one into the switch statement.
        HashSet<Integer> values = new HashSet<Integer>();
        for (EnumFieldDescriptor field : ed.getFields().values()) {
            if( !values.contains(field.getValue()) ) {
                p("   case "+field.getValue()+":");
                p("      return "+field.getName()+";");
                values.add(field.getValue());
            }
            
        }
        p("   default:");
        p("      return null;");
        p("   }");
        p("}");
        
        
        unindent();
        p("}");
        p();
    }


    private boolean isPrimitive(FieldDescriptor field) {
        return field.isNumberType() || field.getType()==FieldDescriptor.BOOL_TYPE;
    }
    
    private String javaCollectionType(FieldDescriptor field) {
        if( field.isInteger32Type() ) {
            return "java.lang.Integer";
        }
        if( field.isInteger64Type() ) {
            return "java.lang.Long";
        }
        if( field.getType() == FieldDescriptor.DOUBLE_TYPE ) {
            return "java.lang.Double";
        }
        if( field.getType() == FieldDescriptor.FLOAT_TYPE ) {
            return "java.lang.Float";
        }
        if( field.getType() == FieldDescriptor.STRING_TYPE ) {
            return "java.lang.String";
        }
        if( field.getType() == FieldDescriptor.BYTES_TYPE ) {
            return "com.google.protobuf.ByteString";
        }
        if( field.getType() == FieldDescriptor.BOOL_TYPE ) {
            return "java.lang.Boolean";
        }
        
        TypeDescriptor descriptor = field.getTypeDescriptor();
        return javaType(descriptor);
    }

    private String javaType(FieldDescriptor field) {
        if( field.isInteger32Type() ) {
            return "int";
        }
        if( field.isInteger64Type() ) {
            return "long";
        }
        if( field.getType() == FieldDescriptor.DOUBLE_TYPE ) {
            return "double";
        }
        if( field.getType() == FieldDescriptor.FLOAT_TYPE ) {
            return "float";
        }
        if( field.getType() == FieldDescriptor.STRING_TYPE ) {
            return "java.lang.String";
        }
        if( field.getType() == FieldDescriptor.BYTES_TYPE ) {
            return "com.google.protobuf.ByteString";
        }
        if( field.getType() == FieldDescriptor.BOOL_TYPE ) {
            return "boolean";
        }
        
        TypeDescriptor descriptor = field.getTypeDescriptor();
        return javaType(descriptor);
    }

    private String javaType(TypeDescriptor descriptor) {
        ProtoDescriptor p = descriptor.getProtoDescriptor();
        if( p != proto ) {
            // Try to keep it short..
            String othePackage = javaPackage(p);
            if( equals(othePackage,javaPackage(proto) ) ) {
                return javaClassName(p)+"."+descriptor.getQName();
            }
            // Use the fully qualified class name.
            return othePackage+"."+javaClassName(p)+"."+descriptor.getQName();
        }
        return descriptor.getQName();
    }
    
    private boolean equals(String o1, String o2) {
        if( o1==o2 )
            return true;
        if( o1==null || o2==null )
            return false;
        return o1.equals(o2);
    }

    private String javaClassName(ProtoDescriptor proto) {
        return getOption(proto, "java_outer_classname", uCamel(removeFileExtension(proto.getName())));
    }


    private String javaPackage(ProtoDescriptor proto) {
        String name = proto.getPackageName();
        if( name!=null ) {
            name = name.replace('_', '.');
            name = name.replace('-', '.');
            name = name.replace('/', '.');
        }
        return getOption(proto, "java_package", name);
    }


    // ----------------------------------------------------------------
    // Internal Helper methods
    // ----------------------------------------------------------------

    private void indent() {
        indent++;
    }

    private void unindent() {
        indent--;
    }

    private void p(String line) {
        // Indent...
        for (int i = 0; i < indent; i++) {
            w.print("   ");
        }
        // Then print.
        w.println(line);
    }

    private void p() {
        w.println();
    }

    private String getOption(ProtoDescriptor proto, String optionName, String defaultValue) {
        OptionDescriptor optionDescriptor = proto.getOptions().get(optionName);
        if (optionDescriptor == null) {
            return defaultValue;
        }
        return optionDescriptor.getValue();
    }

    static private String removeFileExtension(String name) {
        return name.replaceAll("\\..*", "");
    }

    static private String uCamel(String name) {
        boolean upNext=true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if( Character.isJavaIdentifierPart(c) && Character.isLetterOrDigit(c)) {
                if( upNext ) {
                    c = Character.toUpperCase(c);
                    upNext=false;
                }
                sb.append(c);
            } else {
                upNext=true;
            }
        }
        return sb.toString();
    }

    static private String lCamel(String name) {
        if( name == null || name.length()<1 )
            return name;
        return uCamel(name.substring(0,1).toLowerCase()+name.substring(1));
    }

    public File getOut() {
        return out;
    }

    public void setOut(File outputDirectory) {
        this.out = outputDirectory;
    }

    public File[] getPath() {
        return path;
    }

    public void setPath(File[] path) {
        this.path = path;
    }
    
}
