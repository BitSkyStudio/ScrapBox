package com.github.industrialcraft.scrapbox.client;

import com.github.industrialcraft.netx.ClientMessage;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.client.InGameScene;
import com.github.industrialcraft.scrapbox.common.net.IConnection;

import java.util.ArrayList;

public class ClientNetXConnection implements IConnection {
    public final NetXClient client;
    public ClientNetXConnection(NetXClient client) {
        this.client = client;
    }
    @Override
    public void send(Object message) {
        try {
            this.client.send(message);
        } catch(Exception e){}
    }
    @Override
    public ArrayList<Object> read() {
        ArrayList<Object> messages = new ArrayList<>();
        while(this.client.visitMessage(new ClientMessage.Visitor() {
            @Override
            public void message(NetXClient user, Object msg) {
                messages.add(msg);
            }
            @Override
            public void disconnect(NetXClient user) {
                ScrapBox.getInstance().setScene(new DisconnectedScene("connection lost"));
            }
            @Override
            public void exception(NetXClient user, Throwable exception) {
                ScrapBox.getInstance().setScene(new DisconnectedScene("exception: " + exception.getLocalizedMessage()));
            }
        }));
        return messages;
    }
    public void waitForConnect(){
        this.client.visitMessage(new ClientMessage.Visitor() {
            @Override
            public void connect(NetXClient user) {
                ConnectingScene scene = (ConnectingScene) ScrapBox.getInstance().getScene();
                ScrapBox.getInstance().setScene(new InGameScene(scene.connection, null, scene.netXClient));
            }
            @Override
            public void exception(NetXClient user, Throwable exception) {
                ScrapBox.getInstance().setScene(new DisconnectedScene("connecting failed: " + exception.getLocalizedMessage()));
            }
        });
    }
}
