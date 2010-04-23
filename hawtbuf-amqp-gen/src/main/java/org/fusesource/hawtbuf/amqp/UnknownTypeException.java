package org.fusesource.hawtbuf.amqp;

public class UnknownTypeException extends Exception {

    private static final long serialVersionUID = 4106181403332534392L;

    public UnknownTypeException(String message) {
        super(message);
    }
}
