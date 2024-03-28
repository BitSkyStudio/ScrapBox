package com.github.industrialcraft.scrapbox.server;

import com.github.industrialcraft.netx.SocketUser;
import com.github.industrialcraft.scrapbox.common.net.IConnection;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerNetXConnection implements IConnection {
    public final SocketUser socket;
    public final ConcurrentLinkedQueue<Object> queue;
    public ServerNetXConnection(SocketUser socket) {
        this.socket = socket;
        this.queue = new ConcurrentLinkedQueue<>();
    }
    @Override
    public void send(Object message) {
        this.socket.send(message, true);
    }
    @Override
    public ArrayList<Object> read() {
        ArrayList<Object> messages = new ArrayList<>();
        Object message = this.queue.poll();
        while(message != null){
            messages.add(message);
            message = this.queue.poll();
        }
        return messages;
    }
}
