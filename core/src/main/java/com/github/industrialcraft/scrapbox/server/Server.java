package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;
import com.github.industrialcraft.scrapbox.server.net.LocalClientConnection;
import com.github.industrialcraft.scrapbox.server.net.LocalServerConnection;
import com.github.industrialcraft.scrapbox.server.net.MessageC2S;
import com.github.industrialcraft.scrapbox.server.net.MessageS2C;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private final ArrayList<Player> players;
    private final ArrayList<GameObject> gameObjects;
    private final ArrayList<GameObject> newGameObjects;
    public final World physics;
    private boolean stopped;
    public Server() {
        this.players = new ArrayList<>();
        this.physics = new World(new Vector2(0, -9.81f), true);
        this.gameObjects = new ArrayList<>();
        this.newGameObjects = new ArrayList<>();
        this.stopped = false;

        this.spawnGameObject(new Vector2(0, 0), FrameGameObject::new);
    }
    public LocalClientConnection joinLocalPlayer(){
        ConcurrentLinkedQueue<MessageS2C> server_side = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MessageC2S> client_side = new ConcurrentLinkedQueue<>();
        this.addPlayer(new Player(new LocalServerConnection(server_side, client_side)));
        return new LocalClientConnection(server_side, client_side);
    }
    private <T extends GameObject> T spawnGameObject(Vector2 position, GameObject.GameObjectSpawner<T> spawner){
        T gameObject = spawner.spawn(position, this);
        this.newGameObjects.add(gameObject);
        return gameObject;
    }
    private void addPlayer(Player player){
        this.players.add(player);
        ArrayList<MessageS2C> messages = new ArrayList<>();
        this.gameObjects.forEach(gameObject -> messages.add(gameObject.create_add_message()));
        this.newGameObjects.forEach(gameObject -> messages.add(gameObject.create_add_message()));
        player.sendAll(messages);
    }
    private void tick(float deltaTime){
        this.gameObjects.addAll(this.newGameObjects);
        sendNewGameObjects();
        this.newGameObjects.clear();
        this.physics.step(deltaTime, 10, 10);
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
        this.gameObjects.forEach(gameObject -> messages.add(gameObject.create_move_message()));
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
