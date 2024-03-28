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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;

public class ServerJoinScene implements IScene {
    private Stage stage;
    private Table table;
    private HashMap<UUID,ServerEntry> entries;
    private LanReceiver receiver;
    @Override
    public void create() {
        entries = new HashMap<>();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        try {
            receiver = new LanReceiver(InetAddress.getByName("230.1.2.3"), 4321, lanMessage -> {
                JsonValue json = new JsonReader().parse(lanMessage.getContent());
                UUID uuid = UUID.fromString(json.getString("id"));
                int port = json.getInt("port");
                entries.put(uuid, new ServerEntry(lanMessage.getAddress().toString(), port));
                reload();
            });
            receiver.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    private void reload(){
        Skin skin = ScrapBox.getInstance().getSkin();
        table.clear();
        for(ServerEntry entry : entries.values()){
            Label label = new Label(entry.address + ":" + entry.port, skin);
            label.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    NetXClient client = new NetXClient(entry.address.replace("/", ""), entry.port, MessageRegistryCreator.create());
                    client.start();
                    ScrapBox.getInstance().setScene(new ConnectingScene(new ClientNetXConnection(client), client));
                }
            });
            table.add(label);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(50f / 255f, 50f / 255f, 50f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
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
