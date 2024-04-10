package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;

import java.io.DataInputStream;
import java.util.*;

public abstract class GameObject {
    public static final double HALF_PI = Math.PI / 2;

    public final Server server;
    public final HashMap<String,Body> bodies;
    private boolean isRemoved;
    protected HashMap<String,ConnectionData> connections;
    private HashMap<Integer,ValueConnection> valueConnections;
    protected HashMap<Integer,Float> defaultValues;
    public Vehicle vehicle;
    private int baseId;
    public UUID uuid;
    private String baseType;
    protected GameObject(Vector2 position, float rotation, Server server){
        this.server = server;
        this.bodies = new HashMap<>();
        this.isRemoved = false;
        this.connections = new HashMap<>();
        this.valueConnections = new HashMap<>();
        this.defaultValues = new HashMap<>();
        new Vehicle().add(this);
        this.uuid = UUID.randomUUID();
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
        float yPosition = this.getBaseBody().getPosition().y;
        if(yPosition < -100 || yPosition > 1000){
            remove();
        }
    }
    public void disconnect(String name){
        ConnectionData connectionData = connections.remove(name);
        connectionData.other.connections.remove(connectionData.otherName);
        server.physics.destroyJoint(connectionData.joint);

        HashSet<GameObject> originalObjects = new HashSet<>(this.vehicle.gameObjects);
        while(!originalObjects.isEmpty()) {
            HashSet<GameObject> closed = new HashSet<>();
            HashSet<GameObject> open = new HashSet<>();
            Vehicle vehicle = new Vehicle();
            vehicle.setMode(this.vehicle.getMode());
            GameObject go = originalObjects.iterator().next();
            originalObjects.remove(go);
            open.add(go);
            while(!open.isEmpty()){
                GameObject nextOpen = open.iterator().next();
                open.remove(nextOpen);
                closed.add(nextOpen);
                originalObjects.remove(nextOpen);
                nextOpen.vehicle = null;
                vehicle.add(nextOpen);
                for(ConnectionData entry : nextOpen.connections.values()){
                    if(!closed.contains(entry.other)){
                        open.add(entry.other);
                    }
                }
            }
        }
    }
    public boolean collidesWith(Body thisBody, Body other){
        return true;
    }
    public boolean isSideUsed(String name){
        return this.connections.containsKey(name);
    }
    public Joint createJoint(String thisName, GameObject other, String otherName){
        RevoluteJointDef joint = new RevoluteJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.getBaseBody();
        joint.localAnchorA.set(this.getConnectionEdges().get(thisName).offset);
        joint.localAnchorB.set(other.getConnectionEdges().get(otherName).offset);
        joint.enableLimit = true;
        //System.out.println(weldCandidate.angle);
        //joint.referenceAngle = (float) -weldCandidate.angle;
        //joint.referenceAngle = (float) Math.PI;
        joint.referenceAngle = (float) (Math.round((other.getBaseBody().getAngle() - this.getBaseBody().getAngle())/HALF_PI)*HALF_PI);
        joint.lowerAngle = 0f;
        joint.upperAngle = 0f;
        return this.server.physics.createJoint(joint);
    }
    public void requestEditorUI(Player player){

    }
    public void handleEditorUIInput(String elementId, String value){
        try{
            int i = Integer.parseInt(elementId);
            float valueFloat = Float.parseFloat(value);
            defaultValues.put(i, valueFloat);
        } catch (Exception e){}
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
            this.baseType = type;
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
        if(connection == null || connection.gameObject.isRemoved()){
            return defaultValues.getOrDefault(id, 0f);
        }
        return connection.get();
    }
    public void createValueConnection(int id, ValueConnection connection){
        this.valueConnections.put(id, connection);
    }
    public void connect(String id, GameObject gameObject, String otherId, Joint joint){
        this.connections.put(id, new ConnectionData(gameObject, otherId, joint));
    }
    public abstract HashMap<String,ConnectionEdge> getConnectionEdges();
    public HashMap<String,GameObjectConnectionEdge> getOpenConnections(){
        HashMap<String,ConnectionEdge> connections = getConnectionEdges();
        for(String used : this.connections.keySet()){
            connections.remove(used);
        }
        HashMap<String,GameObjectConnectionEdge> output = new HashMap<>();
        for(Map.Entry<String, ConnectionEdge> edge : connections.entrySet()){
            output.put(edge.getKey(), new GameObjectConnectionEdge(edge.getValue(), edge.getKey(), this));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameObject that = (GameObject) o;
        return baseId == that.baseId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseId);
    }

    public abstract String getType();

    @FunctionalInterface
    public interface GameObjectSpawner<T extends GameObject>{
        T spawn(Vector2 position, float rotation, Server server);
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
        public final String name;
        public final GameObject gameObject;
        public GameObjectConnectionEdge(ConnectionEdge connectionEdge, String name, GameObject gameObject) {
            this.connectionEdge = connectionEdge;
            this.name = name;
            this.gameObject = gameObject;
        }
        public Vector2 getPosition(){
            return gameObject.getBaseBody().getWorldPoint(connectionEdge.offset);
        }
        public boolean collides(GameObjectConnectionEdge other){
            return this.getPosition().dst(other.getPosition()) < 0.2 && (this.connectionEdge.internal==other.connectionEdge.internal);
        }
    }
    public static class ConnectionData{
        public final GameObject other;
        public final String otherName;
        public final Joint joint;
        public ConnectionData(GameObject other, String otherName, Joint joint) {
            this.other = other;
            this.otherName = otherName;
            this.joint = joint;
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
