package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;
import com.github.industrialcraft.scrapbox.common.net.LocalConnection;
import com.github.industrialcraft.scrapbox.server.game.WheelGameObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    public final ArrayList<Player> players;
    public final HashMap<Integer,GameObject> gameObjects;
    private final ArrayList<GameObject> newGameObjects;
    public final World physics;
    public final Terrain terrain;
    public final ClientWorldManager clientWorldManager;
    private boolean stopped;
    public boolean paused;
    public Server() {
        this.players = new ArrayList<>();
        this.physics = new World(new Vector2(0, -9.81f), true);
        this.terrain = new Terrain(this);
        this.gameObjects = new HashMap<>();
        this.newGameObjects = new ArrayList<>();
        this.clientWorldManager = new ClientWorldManager(this);
        this.stopped = false;
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
        return null;
    }
    private void addPlayer(Player player){
        this.players.add(player);
        ArrayList<Object> messages = new ArrayList<>();
        this.clientWorldManager.addPlayer(player);
        player.send(this.terrain.createMessage());
        player.sendAll(messages);
    }
    private void tick(float deltaTime){
        for(GameObject gameObject : this.newGameObjects){
            this.gameObjects.put(gameObject.getId(), gameObject);
        }
        this.newGameObjects.clear();
        for(GameObject gameObject : this.gameObjects.values()){
            gameObject.tick();
        }
        this.gameObjects.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        if(!paused) {
            for(int i = 0;i < 10;i++) {
                this.physics.step(deltaTime / 10, 10, 10);
            }
        }
        this.clientWorldManager.updatePositions();
        this.players.forEach(Player::tick);
    }
    public void start(){
        new Thread(() -> {
            while(!stopped){
                tick(1f/20f);
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
    }
}
