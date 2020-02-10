package org.orbitrondev.jass.server.Message;

import org.orbitrondev.jass.lib.Message.MessageData;
import org.orbitrondev.jass.lib.Message.MessageErrorData;
import org.orbitrondev.jass.server.Client;

public class MessageError extends Message {
    private MessageErrorData data;

    public MessageError(MessageData rawData) {
        super(rawData);
        data = (MessageErrorData) rawData;
    }

    /**
     * This message type does no processing at all
     */
    @Override
    public void process(Client client) {
    }
}
