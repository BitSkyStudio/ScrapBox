package com.github.industrialcraft.scrapbox.server;

import com.github.industrialcraft.scrapbox.common.net.IServerConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.industrialcraft.scrapbox.common.net.msg.ToggleGamePaused;

import java.util.ArrayList;

public class Player {
    public final Server server;
    public final IServerConnection connection;
    public Player(Server server, IServerConnection connection) {
        this.server = server;
        this.connection = connection;
    }
    public void tick(){
        for(MessageC2S message : this.connection.read()){
            if(message instanceof ToggleGamePaused){
                server.paused = !server.paused;
            }
        }
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
