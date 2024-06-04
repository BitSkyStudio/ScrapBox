package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.industrialcraft.netx.LanReceiver;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.ClientNetXConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageRegistryCreator;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;

public class ServerJoinScene extends StageBasedScreen {
    private HashMap<UUID,ServerEntry> entries;
    private LanReceiver receiver;
    @Override
    public void create() {
        super.create();
        entries = new HashMap<>();
        try {
            receiver = new LanReceiver(InetAddress.getByName("230.1.2.3"), 4321, lanMessage -> {
                JsonValue json = new JsonReader().parse(lanMessage.getContent());
                UUID uuid = UUID.fromString(json.getString("id"));
                int port = json.getInt("port");
                entries.put(uuid, new ServerEntry(lanMessage.getAddress().toString().replaceFirst("/", ""), port));
                reload();
            });
            receiver.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        reload();
    }
    private void reload(){
        Skin skin = ScrapBox.getInstance().getSkin();
        table.clear();
        for(ServerEntry entry : entries.values()){
            TextButton button = new TextButton(entry.address + ":" + entry.port, skin);
            button.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    NetXClient client = new NetXClient(entry.address, entry.port, MessageRegistryCreator.create());
                    client.start();
                    ScrapBox.getInstance().setScene(new ConnectingScene(new ClientNetXConnection(client), client));
                }
            });
            table.add(button);
            table.row();
        }
        TextButton joinIp = new TextButton("Join IP", skin);
        joinIp.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextField input = new TextField("", skin);
                Dialog dialog = new Dialog("Enter Server IP and Port", skin, "dialog") {
                    public void result(Object obj) {
                        if(obj instanceof String){
                            String[] split = input.getText().trim().split(":");
                            NetXClient client = null;
                            try {
                                client = new NetXClient(split[0], Integer.parseInt(split[1]), MessageRegistryCreator.create());
                                client.start();
                                ScrapBox.getInstance().setScene(new ConnectingScene(new ClientNetXConnection(client), client));
                            } catch(Exception e){
                                if(client != null)
                                    client.disconnect();
                                ScrapBox.getInstance().setScene(new DisconnectedScene(e.getLocalizedMessage()));
                            }
                        }
                    }
                };
                dialog.button("Cancel");
                dialog.button("Ok", "");
                dialog.getContentTable().add(input);
                dialog.show(stage);
            }
        });
        table.add(joinIp);
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new MainMenuScene());
            }
        });
        table.add(back);
    }

    @Override
    public void dispose() {
        super.dispose();
        receiver.cancel();
    }

    public static class ServerEntry{
        public final String address;
        public final int port;
        public ServerEntry(String address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}
