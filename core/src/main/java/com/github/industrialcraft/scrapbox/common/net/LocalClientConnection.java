package com.github.industrialcraft.scrapbox.common.net;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalClientConnection implements IClientConnection{
    public final ConcurrentLinkedQueue<MessageS2C> server_side;
    public final ConcurrentLinkedQueue<MessageC2S> client_side;
    public LocalClientConnection(ConcurrentLinkedQueue<MessageS2C> serverSide, ConcurrentLinkedQueue<MessageC2S> clientSide) {
        server_side = serverSide;
        client_side = clientSide;
    }
    @Override
    public void send(MessageC2S message) {
        this.client_side.add(message);
    }
    @Override
    public ArrayList<MessageS2C> read() {
        ArrayList<MessageS2C> messages = new ArrayList<>();
        MessageS2C message = this.server_side.poll();
        while(message != null){
            messages.add(message);
            message = this.server_side.poll();
        }
        return messages;
    }
}
