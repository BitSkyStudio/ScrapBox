package com.github.industrialcraft.scrapbox.server.net;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalServerConnection implements IServerConnection{
    public final ConcurrentLinkedQueue<MessageS2C> server_side;
    public final ConcurrentLinkedQueue<MessageC2S> client_side;
    public LocalServerConnection(ConcurrentLinkedQueue<MessageS2C> serverSide, ConcurrentLinkedQueue<MessageC2S> clientSide) {
        server_side = serverSide;
        client_side = clientSide;
    }
    @Override
    public void send(MessageS2C message) {
        this.server_side.add(message);
    }
    @Override
    public ArrayList<MessageC2S> read() {
        ArrayList<MessageC2S> messages = new ArrayList<>();
        MessageC2S message = this.client_side.poll();
        while(message != null){
            messages.add(message);
            message = this.client_side.poll();
        }
        return messages;
    }
}
