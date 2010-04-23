package org.fusesource.hawtbuf.proto;

import java.io.IOException;
import java.io.OutputStream;

import org.fusesource.hawtbuf.Buffer;


public interface MessageBuffer<B, MB extends MessageBuffer> extends PBMessage<B, MB> {
   
    public int serializedSizeUnframed();
    
    public int serializedSizeFramed();

    public Buffer toUnframedBuffer();
    
    public Buffer toFramedBuffer();

    public byte[] toUnframedByteArray();
   
    public byte[] toFramedByteArray();
    
    public void writeUnframed(CodedOutputStream output) throws java.io.IOException;
    
    public void writeFramed(CodedOutputStream output) throws java.io.IOException;
    
    public void writeUnframed(OutputStream output) throws IOException;
    
    public void writeFramed(OutputStream output) throws java.io.IOException;    

}
