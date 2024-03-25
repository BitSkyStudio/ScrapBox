package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.github.industrialcraft.scrapbox.common.net.msg.DeleteGameObject;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;
import com.github.industrialcraft.scrapbox.common.net.LocalClientConnection;
import com.github.industrialcraft.scrapbox.common.net.LocalServerConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
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
    private boolean stopped;
    public boolean paused;
    public Server() {
        this.players = new ArrayList<>();
        this.physics = new World(new Vector2(0, -9.81f), true);
        this.terrain = new Terrain(this);
        this.gameObjects = new HashMap<>();
        this.newGameObjects = new ArrayList<>();
        this.stopped = false;
    }
    public LocalClientConnection joinLocalPlayer(){
        ConcurrentLinkedQueue<MessageS2C> server_side = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MessageC2S> client_side = new ConcurrentLinkedQueue<>();
        this.addPlayer(new Player(this, new LocalServerConnection(server_side, client_side)));
        return new LocalClientConnection(server_side, client_side);
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
        ArrayList<MessageS2C> messages = new ArrayList<>();
        this.gameObjects.values().forEach(gameObject -> messages.add(gameObject.create_add_message()));
        this.newGameObjects.forEach(gameObject -> messages.add(gameObject.create_add_message()));
        player.send(this.terrain.createMessage());
        player.sendAll(messages);
    }
    private void tick(float deltaTime){
        for(GameObject gameObject : this.newGameObjects){
            this.gameObjects.put(gameObject.id, gameObject);
        }
        sendNewGameObjects();
        this.newGameObjects.clear();
        for(GameObject gameObject : this.gameObjects.values()){
            gameObject.tick();
            if(gameObject.isRemoved()){
                DeleteGameObject deleteGameObject = new DeleteGameObject(gameObject.id);
                this.players.forEach(player -> player.send(deleteGameObject));
            }
        }
        this.gameObjects.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        if(!paused) {
            for(int i = 0;i < 10;i++) {
                this.physics.step(deltaTime / 10, 10, 10);
            }
        }
        sendUpdatedPositions();
        this.players.forEach(Player::tick);
    }
    private void sendNewGameObjects(){
        ArrayList<MessageS2C> messages = new ArrayList<>();
        this.newGameObjects.forEach(gameObject -> messages.add(gameObject.create_add_message()));
        this.players.forEach(player -> player.sendAll(messages));
    }
    private void sendUpdatedPositions(){
        ArrayList<MessageS2C> messages = new ArrayList<>();
        this.gameObjects.values().forEach(gameObject -> messages.add(gameObject.create_move_message()));
        this.players.forEach(player -> player.sendAll(messages));
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
