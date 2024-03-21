package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameObject {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    public final Server server;
    public final int id;
    public final Body body;
    private boolean isRemoved;
    private HashMap<String,GameObject> connections;
    protected GameObject(Vector2 position, Server server){
        this.server = server;
        this.id = idGenerator.addAndGet(1);
        this.body = server.physics.createBody(create_body_def(position));
        this.body.setUserData(this);
        this.add_fixtures();
        this.isRemoved = false;
        this.connections = new HashMap<>();
    }
    protected BodyDef create_body_def(Vector2 position){
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        return bodyDef;
    }
    public void remove(){
        if(!isRemoved){
            server.physics.destroyBody(this.body);
        }
        this.isRemoved = true;
    }
    public boolean isRemoved(){
        return this.isRemoved;
    }
    public void tick(){
        if(this.body.getPosition().y < -100){
            remove();
        }
    }

    protected abstract void add_fixtures();
    public abstract String get_type();

    public abstract HashMap<String,ConnectionEdge> getConnectionEdges();
    public HashMap<String,GameObjectConnectionEdge> getOpenConnections(){
        HashMap<String,ConnectionEdge> connections = getConnectionEdges();
        for(String used : this.connections.keySet()){
            connections.remove(used);
        }
        HashMap<String,GameObjectConnectionEdge> output = new HashMap<>();
        for(Map.Entry<String, ConnectionEdge> edge : connections.entrySet()){
            output.put(edge.getKey(), new GameObjectConnectionEdge(edge.getValue(), this));
        }
        return output;
    }
    public ArrayList<WeldCandidate> getPossibleWelds(){
        ArrayList<WeldCandidate> weldCandidates = new ArrayList<>();
        for(Map.Entry<String, GameObjectConnectionEdge> edge1 : this.getOpenConnections().entrySet()){
            for(GameObject other : this.server.gameObjects.values()){
                if(other == this){
                    continue;
                }
                for(Map.Entry<String, GameObjectConnectionEdge> edge2: other.getOpenConnections().entrySet()){
                    if(edge1.getValue().collides(edge2.getValue())){
                        weldCandidates.add(new WeldCandidate(edge1.getValue(), edge1.getKey(), edge2.getValue(), edge2.getKey()));
                    }
                }
            }
        }
        return weldCandidates;
    }

    public void setGhost(boolean isGhost){
        Filter filter = new Filter();
        if(isGhost){
            filter.categoryBits = 0;
            filter.maskBits = 0;
        }
        this.body.getFixtureList().forEach(fixture -> fixture.setFilterData(filter));
    }
    public MessageS2C create_add_message(){
        return new AddGameObjectMessage(this.id, this.get_type(), this.body.getPosition(), this.body.getAngle());
    }
    public MessageS2C create_move_message(){
        return new MoveGameObjectMessage(this.id, this.body.getPosition(), this.body.getAngle());
    }
    @FunctionalInterface
    public interface GameObjectSpawner<T extends GameObject>{
        T spawn(Vector2 position, Server server);
    }
    public static class ConnectionEdge{
        public final Vector2 offset;
        public final float angle;
        public ConnectionEdge(Vector2 offset, float angle) {
            this.offset = offset;
            this.angle = angle;
        }

    }
    public static class GameObjectConnectionEdge{
        public final ConnectionEdge connectionEdge;
        public final GameObject gameObject;
        public GameObjectConnectionEdge(ConnectionEdge connectionEdge, GameObject gameObject) {
            this.connectionEdge = connectionEdge;
            this.gameObject = gameObject;
        }
        public Vector2 getPosition(){
            return gameObject.body.getWorldPoint(connectionEdge.offset);
        }
        public float getAngle(){
            return this.gameObject.body.getAngle() + connectionEdge.angle;
        }
        public boolean collides(GameObjectConnectionEdge other){
            final double TWO_PI = Math.PI * 2;
            return this.getPosition().dst(other.getPosition()) < 0.2/* && Math.abs(this.getAngle()-((other.getAngle()+Math.PI)%TWO_PI)+2*TWO_PI)%Math.PI < Math.PI/10*/;
        }
    }
    public static class WeldCandidate{
        public final GameObjectConnectionEdge first;
        public final String keyFirst;
        public final GameObjectConnectionEdge second;
        public final String keySecond;
        public WeldCandidate(GameObjectConnectionEdge first, String keyFirst, GameObjectConnectionEdge second, String keySecond) {
            this.first = first;
            this.keyFirst = keyFirst;
            this.second = second;
            this.keySecond = keySecond;
        }
    }
}
