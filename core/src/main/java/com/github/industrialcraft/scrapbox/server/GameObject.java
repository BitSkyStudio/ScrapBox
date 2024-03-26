package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameObject {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    public final Server server;
    public final int id;
    public final HashMap<String,Body> bodies;
    private boolean isRemoved;
    private HashMap<String,GameObject> connections;
    public Vehicle vehicle;
    protected GameObject(Vector2 position, Server server){
        this.server = server;
        this.id = idGenerator.addAndGet(1);
        this.bodies = new HashMap<>();
        this.isRemoved = false;
        this.connections = new HashMap<>();
        new Vehicle().add(this);
    }
    public void setLocked(boolean isStatic){
        if(isStatic) {
            this.bodies.forEach((s, body) -> body.setType(BodyDef.BodyType.StaticBody));
        } else {
            this.bodies.forEach((s, body) -> body.setType(BodyDef.BodyType.DynamicBody));
        }
    }
    public void remove(){
        if(!isRemoved){
            this.destroy();
        }
        this.isRemoved = true;
    }
    public void destroy(){
        this.bodies.forEach((s, body) -> server.physics.destroyBody(body));
        this.server.clientWorldManager.removeObject(this);
    }
    public boolean isRemoved(){
        return this.isRemoved;
    }
    public void tick(){
        if(this.getBaseBody().getPosition().y < -100){
            remove();
        }
    }
    public boolean isSideUsed(String name){
        return this.connections.containsKey(name);
    }
    public void createJoint(GameObjectConnectionEdge self, GameObjectConnectionEdge other){
        RevoluteJointDef joint = new RevoluteJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.gameObject.getBaseBody();
        joint.localAnchorA.set(self.connectionEdge.offset);
        joint.localAnchorB.set(other.connectionEdge.offset);
        joint.enableLimit = true;
        //System.out.println(weldCandidate.angle);
        //joint.referenceAngle = (float) -weldCandidate.angle;
        //joint.referenceAngle = (float) Math.PI;
        joint.referenceAngle = other.gameObject.getBaseBody().getAngle() - this.getBaseBody().getAngle();
        joint.lowerAngle = 0f;
        joint.upperAngle = 0f;
        this.server.physics.createJoint(joint);
    }
    public Body getBody(String name){
        return this.bodies.get(name);
    }
    public Body getBaseBody(){
        return this.bodies.get("base");
    }
    public void setBody(String name, String type, Body body){
        //todo: overwrites
        this.bodies.put(name, body);
        body.setUserData(this);
        server.clientWorldManager.addBody(this, body, type);
    }
    public float getMass(){
        float mass = 0;
        for(Body body : this.bodies.values()){
            mass += body.getMass();
        }
        return mass;
    }
    public Vector2 getCenterOfMass(){
        float totalMass = this.getMass();
        Vector2 center = new Vector2();
        for (Body body : this.bodies.values()) {
            Vector2 localCenter = body.getWorldCenter();
            center.add(localCenter.scl(body.getMass() / totalMass));
        }
        return center;
    }

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
                    if(other.isSideUsed(edge2.getKey())){
                        continue;
                    }
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
        this.bodies.forEach((s, body) -> body.getFixtureList().forEach(fixture -> fixture.setFilterData(filter)));
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
            return gameObject.getBaseBody().getWorldPoint(connectionEdge.offset);
        }
        public float getAngle(){
            return this.gameObject.getBaseBody().getAngle() + connectionEdge.angle;
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
