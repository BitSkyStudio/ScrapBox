package com.github.industrialcraft.scrapbox.server;

import com.github.industrialcraft.scrapbox.common.net.IServerConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

import java.util.ArrayList;

public class Player {
    public final IServerConnection connection;
    public Player(IServerConnection connection) {
        this.connection = connection;
    }
    public void tick(){

    }
    public void send(MessageS2C message){
        this.connection.send(message);
    }
    public void sendAll(ArrayList<MessageS2C> messages){
        for(MessageS2C message : messages){
            this.connection.send(message);
        }
    }
}
