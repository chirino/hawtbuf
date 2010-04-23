package org.fusesource.hawtbuf.amqp;

import java.util.LinkedList;

import org.fusesource.hawtbuf.amqp.jaxb.schema.Choice;

public class AmqpChoice {

    LinkedList<Choice> choices = new LinkedList<Choice>();
    public void parseFromChoice(Choice choice) {
        choices.add(choice);
    }
}
