package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class GameObject {
    public static final double HALF_PI = Math.PI / 2;

    public final Server server;
    public final HashMap<String,Body> bodies;
    private boolean isRemoved;
    private HashMap<String,GameObject> connections;
    private HashMap<Integer,ValueConnection> valueConnections;
    public Vehicle vehicle;
    private int baseId;
    protected GameObject(Vector2 position, Server server){
        this.server = server;
        this.bodies = new HashMap<>();
        this.isRemoved = false;
        this.connections = new HashMap<>();
        this.valueConnections = new HashMap<>();
        new Vehicle().add(this);
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
        joint.referenceAngle = (float) (Math.round((other.gameObject.getBaseBody().getAngle() - this.getBaseBody().getAngle())/HALF_PI)*HALF_PI);
        joint.lowerAngle = 0f;
        joint.upperAngle = 0f;
        this.server.physics.createJoint(joint);
    }
    public void requestEditorUI(Player player){

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
        boolean base = name.equals("base");
        int id = server.clientWorldManager.addBody(this, body, type, base);
        if(base){
            this.baseId = id;
        }
    }
    public int getId(){
        return this.baseId;
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
    public float getValueOnOutput(int id){
        return 0;
    }
    public float getValueOnInput(int id){
        ValueConnection connection = valueConnections.get(id);
        if(connection == null){
            return 0;
        }
        if(connection.gameObject.isRemoved()){
            return 0;
        }
        return connection.get();
    }
    public void createValueConnection(int id, ValueConnection connection){
        this.valueConnections.put(id, connection);
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
    public void setMode(EObjectInteractionMode mode){
        BodyDef.BodyType type;
        Filter filter = new Filter();
        if(mode == EObjectInteractionMode.Static){
            type = BodyDef.BodyType.StaticBody;
        } else {
            type = BodyDef.BodyType.DynamicBody;
            if(mode == EObjectInteractionMode.Ghost){
                filter.categoryBits = 0;
                filter.maskBits = 0;
            }
        }
        this.bodies.forEach((s, body) -> {
            body.setType(type);
            body.getFixtureList().forEach(fixture -> fixture.setFilterData(filter));
        });
    }
    @FunctionalInterface
    public interface GameObjectSpawner<T extends GameObject>{
        T spawn(Vector2 position, Server server);
    }
    public static class ConnectionEdge{
        public final Vector2 offset;
        public final boolean internal;
        public ConnectionEdge(Vector2 offset, boolean internal) {
            this.offset = offset;
            this.internal = internal;
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
        public boolean collides(GameObjectConnectionEdge other){
            return this.getPosition().dst(other.getPosition()) < 0.2 && (this.connectionEdge.internal==other.connectionEdge.internal);
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
    public static class ValueConnection{
        public final GameObject gameObject;
        public final int id;
        public ValueConnection(GameObject gameObject, int id) {
            this.gameObject = gameObject;
            this.id = id;
        }
        public float get(){
            return gameObject.getValueOnOutput(id);
        }
    }
}
