package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.industrialcraft.netx.LANBroadcaster;
import com.github.industrialcraft.netx.NetXServer;
import com.github.industrialcraft.netx.ServerMessage;
import com.github.industrialcraft.netx.SocketUser;
import com.github.industrialcraft.scrapbox.common.net.MessageRegistryCreator;
import com.github.industrialcraft.scrapbox.server.game.BalloonGameObject;
import com.github.industrialcraft.scrapbox.server.game.ControllerGameObject;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;
import com.github.industrialcraft.scrapbox.common.net.LocalConnection;
import com.github.industrialcraft.scrapbox.server.game.WheelGameObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    public final ArrayList<Player> players;
    public final HashMap<Integer,GameObject> gameObjects;
    private final ArrayList<GameObject> newGameObjects;
    public final World physics;
    public final Terrain terrain;
    public final ClientWorldManager clientWorldManager;
    public final NetXServer networkServer;
    private boolean stopped;
    public boolean paused;
    private int tickCount;
    private final UUID uuid;
    public Server() {
        this.uuid = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.physics = new World(new Vector2(0, -9.81f), true);
        this.terrain = new Terrain(this);
        this.gameObjects = new HashMap<>();
        this.newGameObjects = new ArrayList<>();
        this.clientWorldManager = new ClientWorldManager(this);
        this.networkServer = new NetXServer(0, MessageRegistryCreator.create());
        this.networkServer.start();
        this.stopped = false;
        this.tickCount = 0;
    }
    public LocalConnection joinLocalPlayer(){
        ConcurrentLinkedQueue<Object> write = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Object> read = new ConcurrentLinkedQueue<>();
        this.addPlayer(new Player(this, new LocalConnection(write, read)));
        return new LocalConnection(read, write);
    }
    public  <T extends GameObject> T spawnGameObject(Vector2 position, GameObject.GameObjectSpawner<T> spawner){
        T gameObject = spawner.spawn(position, this);
        this.newGameObjects.add(gameObject);
        return gameObject;
    }
    public GameObject spawnGameObject(Vector2 position, String type){
        if(type.equals("frame")){
            return spawnGameObject(position, FrameGameObject::new);
        }
        if(type.equals("wheel")){
            return spawnGameObject(position, WheelGameObject::new);
        }
        if(type.equals("balloon")){
            return spawnGameObject(position, BalloonGameObject::new);
        }
        if(type.equals("controller")){
            return spawnGameObject(position, ControllerGameObject::new);
        }
        throw new IllegalArgumentException("unknown type " + type);
    }
    private void addPlayer(Player player){
        this.players.add(player);
        ArrayList<Object> messages = new ArrayList<>();
        this.clientWorldManager.addPlayer(player);
        player.send(this.terrain.createMessage());
        player.sendAll(messages);
    }
    private void tick(float deltaTime) throws Exception{
        for(GameObject gameObject : this.newGameObjects){
            this.gameObjects.put(gameObject.getId(), gameObject);
        }
        this.newGameObjects.clear();
        this.gameObjects.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        if(!paused) {
            for(GameObject gameObject : this.gameObjects.values()){
                gameObject.tick();
            }
            for(int i = 0;i < 10;i++) {
                this.physics.step(deltaTime / 10, 10, 10);
            }
        }
        this.clientWorldManager.updatePositions();
        this.players.forEach(Player::tick);
        this.players.removeIf(Player::isDisconnected);
        while(this.networkServer.visitMessage(new ServerMessage.Visitor() {
            @Override
            public void connect(SocketUser user) {
                Player player = new Player(Server.this, new ServerNetXConnection(user));
                addPlayer(player);
                user.setUserData(player);
            }
            @Override
            public void disconnect(SocketUser user) {
                ((Player)user.getUserData()).disconnect();
            }
            @Override
            public void message(SocketUser user, Object msg) {
                ((ServerNetXConnection)((Player)user.getUserData()).connection).queue.add(msg);
            }
        }));
        if(tickCount%20==1){
            InetSocketAddress address = networkServer.getAddress();
            if(address != null) {
                JsonValue json = new JsonValue(JsonValue.ValueType.object);
                json.addChild("id", new JsonValue(uuid.toString()));
                json.addChild("port", new JsonValue(address.getPort()));
                LANBroadcaster.broadcast(json.toJson(JsonWriter.OutputType.json), InetAddress.getByName("230.1.2.3"), 4321);
            }
        }
    }
    public void start(){
        new Thread(() -> {
            while(!stopped){
                try {
                    tick(1f / 20f);
                } catch(Exception e){
                    e.printStackTrace();
                    stop();
                }
                tickCount++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public void stop(){
        this.stopped = true;
        this.networkServer.close();
    }
}
