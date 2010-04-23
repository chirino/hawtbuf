package org.fusesource.hawtbuf.amqp;

import org.fusesource.hawtbuf.amqp.jaxb.schema.Exception;

public class AmqpException {

    String errorCode; 
    String name;
    
    public void parseFromException(Exception exception)
    {
        errorCode = exception.getErrorCode();
        name = exception.getName();
        //TODO exception.getDoc()
    }
}
