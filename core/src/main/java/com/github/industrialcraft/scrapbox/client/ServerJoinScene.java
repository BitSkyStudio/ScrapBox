package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.codedisaster.steamworks.*;
import com.github.industrialcraft.netx.LanReceiver;
import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.netx.timeout.PingMessage;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageRegistryCreator;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
        TextButton joinSteam = new TextButton("Join Steam", skin);
        joinSteam.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextField input = new TextField("", skin);
                Dialog dialog = new Dialog("Enter SteamID", skin, "dialog") {
                    public void result(Object obj) {
                        if(obj instanceof String){
                            try {
                                SteamID steamID = SteamID.createFromNativeHandle(Long.parseLong(input.getText().trim()));
                                AtomicReference<SteamNetworking> steamNetworking = new AtomicReference(null);
                                steamNetworking.set(new SteamNetworking(new SteamNetworkingCallback() {
                                    @Override
                                    public void onP2PSessionConnectFail(SteamID steamID, SteamNetworking.P2PSessionError p2PSessionError) {

                                    }
                                    @Override
                                    public void onP2PSessionRequest(SteamID steamID) {
                                        steamNetworking.get().acceptP2PSessionWithUser(steamID);
                                    }
                                }));
                                ScrapBox.getInstance().setScene(new InGameScene(new IConnection(){
                                    @Override
                                    public void send(Object message) {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        MessageRegistry.MessageDescriptor md = MessageRegistryCreator.CACHE.byClass(message.getClass());
                                        if (md == null) {
                                            throw new RuntimeException("attempting to serialize message with no assigned id, class: " + message.getClass().getSimpleName());
                                        } else if (md.writer == null) {
                                            throw new RuntimeException("trying to send packet without writer implemented, class: " + message.getClass().getSimpleName());
                                        } else {
                                            DataOutputStream dos = new DataOutputStream(stream);
                                            try {
                                                dos.writeInt(md.getId());
                                                md.writer.write(message, dos);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        try {
                                            steamNetworking.get().sendP2PPacket(steamID, ByteBuffer.wrap(stream.toByteArray()), SteamNetworking.P2PSend.Reliable, 0);
                                        } catch (SteamException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    @Override
                                    public ArrayList<Object> read() {
                                        ArrayList<Object> messages = new ArrayList<>();
                                        int[] size = new int[]{0};
                                        while(steamNetworking.get().isP2PPacketAvailable(0, size)) {
                                            System.out.println("size: " + size[0]);
                                            ByteBuffer buffer = ByteBuffer.allocate(8192);
                                            try {
                                                steamNetworking.get().readP2PPacket(steamID, buffer, 0);
                                            } catch (SteamException e) {
                                                throw new RuntimeException(e);
                                            }
                                            ByteArrayInputStream stream = new ByteArrayInputStream(buffer.array());
                                            DataInputStream dis = new DataInputStream(stream);
                                            int id = 0;
                                            try {
                                                id = dis.readInt();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            MessageRegistry.MessageDescriptor descriptor = MessageRegistryCreator.CACHE.byID(id);
                                            if (descriptor == null) {
                                                throw new RuntimeException("unknown packet id: " + id);
                                            } else if (descriptor.reader == null) {
                                                throw new RuntimeException("received packet without reader implemented, id: " + id);
                                            } else {
                                                try {
                                                    messages.add(descriptor.reader.read(new DataInputStream(dis)));
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                        return messages;
                                    }
                                }, null, null));
                            } catch(Exception e){
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
        table.add(joinSteam);
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
