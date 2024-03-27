package com.github.industrialcraft.scrapbox.common.net;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalConnection implements IConnection {
    public final ConcurrentLinkedQueue<Object> write;
    public final ConcurrentLinkedQueue<Object> read;
    public LocalConnection(ConcurrentLinkedQueue<Object> serverSide, ConcurrentLinkedQueue<Object> clientSide) {
        write = serverSide;
        read = clientSide;
    }
    @Override
    public void send(Object message) {
        this.write.add(message);
    }
    @Override
    public ArrayList<Object> read() {
        ArrayList<Object> messages = new ArrayList<>();
        Object message = this.read.poll();
        while(message != null){
            messages.add(message);
            message = this.read.poll();
        }
        return messages;
    }
}
