package com.github.industrialcraft.scrapbox.server;

import clipper2.Clipper;
import clipper2.core.PathD;
import clipper2.core.PointD;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.Material;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.common.net.msg.OpenGameObjectEditUI;
import com.github.industrialcraft.scrapbox.common.net.msg.PlaySoundMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.SendConnectionListData;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public abstract class GameObject {
    public static final double HALF_PI = Math.PI / 2;

    public final Server server;
    public final HashMap<String,Body> bodies;
    private boolean isRemoved;
    public HashMap<String,ConnectionData> connections;
    protected HashMap<Integer,ValueConnection> valueConnections;
    public HashMap<UUID, GearConnectionData> gearConnections;
    protected HashMap<Integer,Float> defaultValues;
    public Vehicle vehicle;
    private int baseId;
    public UUID uuid;
    public HashSet<Player> uiViewers;
    public float health;
    public EnumMap<EDamageType, Float> damageModifiers;
    public final GameObjectConfig config;
    protected GameObject(Vector2 position, float rotation, Server server, GameObjectConfig config){
        this.server = server;
        this.config = config;
        this.bodies = new HashMap<>();
        this.isRemoved = false;
        this.connections = new HashMap<>();
        this.valueConnections = new HashMap<>();
        this.defaultValues = new HashMap<>();
        new Vehicle().add(this);
        this.uuid = UUID.randomUUID();
        this.uiViewers = new HashSet<>();
        this.health = getMaxHealth();
        this.damageModifiers = new EnumMap<>(EDamageType.class);
        this.gearConnections = new HashMap<>();
    }
    public void playSound(String sound){
        int id = server.soundIdGenerator++;
        PlaySoundMessage message = new PlaySoundMessage(id, sound, getId(), new Vector2(), false);
        server.players.forEach(player -> player.send(message));
    }
    public Joint getGearJoint(){
        return null;
    }
    public String getGearJointBody(){
        return null;
    }
    public void connectGearJoint(GameObject other, int thisRatio, int otherRatio){
        if(this == other || this.vehicle != other.vehicle)
            return;
        Joint thisJoint = getGearJoint();
        Joint otherJoint = other.getGearJoint();
        if(thisJoint == null || otherJoint == null)
            return;
        if(gearConnections.containsKey(other.uuid))
            disconnectGearJoint(other);
        GearJointDef gearJointDef = new GearJointDef();
        gearJointDef.bodyA = getBody(getGearJointBody());
        gearJointDef.bodyB = other.getBody(other.getGearJointBody());
        gearJointDef.joint1 = thisJoint;
        gearJointDef.joint2 = otherJoint;
        gearJointDef.ratio = ((float)thisRatio)/((float)otherRatio);
        GearJoint gearJoint = (GearJoint) server.physics.createJoint(gearJointDef);
        this.gearConnections.put(other.uuid, new GearConnectionData(other, thisRatio, otherRatio, gearJoint));
        other.gearConnections.put(this.uuid, new GearConnectionData(this, otherRatio, thisRatio, gearJoint));
    }
    public void disconnectGearJoint(GameObject other){
        GearConnectionData gearConnectionData = gearConnections.get(other.uuid);
        if(gearConnectionData != null){
            this.gearConnections.remove(other.uuid);
            other.gearConnections.remove(this.uuid);
            server.physics.destroyJoint(gearConnectionData.joint);
        }
    }
    public float getMaxHealth(){
        return 100;
    }
    public void damage(float amount, EDamageType damageType){
        this.health -= amount * this.damageModifiers.getOrDefault(damageType, 1f);
        if(this.health > getMaxHealth())
            health = getMaxHealth();
        if(health <= 0){
            this.remove();
        }
    }
    public void setDamageModifier(EDamageType damageType, float modifier){
        this.damageModifiers.put(damageType, modifier);
    }
    public void load(DataInputStream stream) throws IOException{
        this.health = stream.readFloat();
        int valueConnectionSize = stream.readInt();
        valueConnections.clear();
        for(int i = 0;i < valueConnectionSize;i++){
            int id = stream.readInt();
            GameObject gameObject = server.getGameObjectByUUID(new UUID(stream.readLong(), stream.readLong()));
            int otherId = stream.readInt();
            if(gameObject != null)
                valueConnections.put(id, new ValueConnection(gameObject, otherId));
        }
        int defaultValuesSize = stream.readInt();
        defaultValues.clear();
        for(int i = 0;i < defaultValuesSize;i++) {
            defaultValues.put(stream.readInt(), stream.readFloat());
        }
        int gearConnectionSize = stream.readInt();
        for(int i = 0;i < gearConnectionSize;i++){
            UUID otherId = new UUID(stream.readLong(), stream.readLong());
            GameObject other = server.getGameObjectByUUID(otherId);
            connectGearJoint(other, stream.readInt(), stream.readInt());
        }
    }
    public void save(DataOutputStream stream) throws IOException {
        stream.writeFloat(health);
        valueConnections.entrySet().removeIf(entry -> entry.getValue() == null);
        stream.writeInt(valueConnections.size());
        for(Map.Entry<Integer, ValueConnection> entry : valueConnections.entrySet()){
            stream.writeInt(entry.getKey());
            stream.writeLong(entry.getValue().gameObject.uuid.getMostSignificantBits());
            stream.writeLong(entry.getValue().gameObject.uuid.getLeastSignificantBits());
            stream.writeInt(entry.getValue().id);
        }
        stream.writeInt(defaultValues.size());
        for(Map.Entry<Integer, Float> entry : defaultValues.entrySet()){
            stream.writeInt(entry.getKey());
            stream.writeFloat(entry.getValue());
        }
        stream.writeInt(gearConnections.size());
        for(GearConnectionData gearConnection : gearConnections.values()){
            stream.writeLong(gearConnection.other.uuid.getMostSignificantBits());
            stream.writeLong(gearConnection.other.uuid.getLeastSignificantBits());
            stream.writeInt(gearConnection.thisRatio);
            stream.writeInt(gearConnection.otherRatio);
        }
    }
    public void remove(){
        this.isRemoved = true;
        this.vehicle.gameObjects.remove(this);
        for(UUID id : this.gearConnections.keySet().toArray(UUID[]::new)){
            disconnectGearJoint(server.getGameObjectByUUID(id));
        }
    }
    public void destroy(){
        this.bodies.forEach((s, body) -> server.physics.destroyBody(body));
        this.connections.forEach((s, connectionData) -> {
            connectionData.other.connections.remove(connectionData.otherName);
        });
        this.server.clientWorldManager.removeObject(this);
    }
    public boolean isRemoved(){
        return this.isRemoved;
    }
    public void tick(){
        if(Math.abs(this.getBaseBody().getPosition().y) > 1000){
            remove();
        }
    }
    public void internalTick(){
        for(Body body : bodies.values()){
            if(body.getMassData().center.y + body.getWorldCenter().y < -100){
                float area = 0;
                for(Fixture fixture : body.getFixtureList()){
                    Shape shape = fixture.getShape();
                    if(shape instanceof CircleShape){
                        area += (float) (Math.PI * Math.pow(shape.getRadius(), 2));
                    }
                    if(shape instanceof PolygonShape){
                        PolygonShape polygonShape = (PolygonShape) shape;
                        PathD path = new PathD();
                        Vector2 p = new Vector2();
                        for(int i = 0;i < polygonShape.getVertexCount();i++){
                            polygonShape.getVertex(i, p);
                            path.add(new PointD(p.x, p.y));
                        }
                        area += (float) Clipper.Area(path);
                    }
                }
                body.applyForceToCenter(new Vector2(0, area*15), true);
                float drag = (float) (Math.pow(body.getLinearVelocity().len(), 2) * 0.3);
                if(drag > 0)
                    body.applyForceToCenter(body.getLinearVelocity().nor().scl(-drag), true);
            }
        }
    }
    public void disconnect(String name){
        ConnectionData connectionData = connections.remove(name);
        connectionData.other.connections.remove(connectionData.otherName);
        server.physics.destroyJoint(connectionData.joint);

        HashSet<GameObject> originalObjects = new HashSet<>(this.vehicle.gameObjects);
        HashSet<GameObject> originalObjects2 = new HashSet<>(this.vehicle.gameObjects);
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
        for(GameObject gameObject : originalObjects2){
            for(UUID id : gameObject.gearConnections.keySet().toArray(UUID[]::new)){
                GameObject other = server.getGameObjectByUUID(id);
                if(gameObject.vehicle != other.vehicle)
                    gameObject.disconnectGearJoint(other);
            }
        }
    }
    public boolean collidesWith(Fixture thisFixture, Fixture other){
        return true;
    }
    public void onCollision(Fixture thisFixture, Fixture other){

    }
    public boolean isSideUsed(String name){
        return this.connections.containsKey(name);
    }
    public ArrayList<EditorUIRow> createEditorUI(){
        return null;
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
        MassData massData = body.getMassData();
        //massData.I *= 0.9f;
        body.setMassData(massData);
        //body.setAngularDamping(3);

        this.bodies.put(name, body);
        body.setUserData(this);
        boolean base = name.equals("base");
        int id = server.clientWorldManager.addBody(this, body, type, base && getType() != null, base && getGearJointBody() != null);
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
        if(connection != null && (connection.gameObject.isRemoved() || connection.gameObject.vehicle != this.vehicle)){
            valueConnections.remove(id);
            updateUI();
            connection = null;
        }
        return connection==null?defaultValues.getOrDefault(id, 0f):connection.get();
    }
    public boolean isInputFilled(int id){
        ValueConnection connection = valueConnections.get(id);
        return !(connection == null || connection.gameObject.isRemoved());
    }
    public void createValueConnection(int id, ValueConnection connection){
        this.valueConnections.put(id, connection);
    }
    public void destroyValueConnection(int id){
        this.valueConnections.remove(id);
    }
    public void connect(String id, GameObject gameObject, String otherId, Joint joint){
        this.connections.put(id, new ConnectionData(gameObject, otherId, joint));
    }
    public void updateUI(){
        ArrayList<EditorUIRow> rows = createEditorUI();
        if(rows == null)
            return;
        SetGameObjectEditUIData message = new SetGameObjectEditUIData(this.getId(), rows);
        this.uiViewers.forEach(player -> player.send(message));
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
        int highestPriority = -1;
        for(Map.Entry<String, GameObjectConnectionEdge> edge1 : this.getOpenConnections().entrySet()){
            for(GameObject other : this.server.gameObjects.values()){
                if(other == this){
                    continue;
                }
                for (Map.Entry<String, GameObjectConnectionEdge> edge2 : other.getOpenConnections().entrySet()) {
                    if (edge1.getValue().collides(edge2.getValue())) {
                        int newPriority = Math.max(edge1.getValue().connectionEdge.priority, edge2.getValue().connectionEdge.priority);
                        if(newPriority >= highestPriority) {
                            if(newPriority > highestPriority)
                                weldCandidates.clear();
                            highestPriority = newPriority;
                            weldCandidates.add(new WeldCandidate(edge1.getValue(), edge1.getKey(), edge2.getValue(), edge2.getKey()));
                        }
                    }
                }
            }
        }
        return weldCandidates;
    }
    private EObjectInteractionMode localMode = EObjectInteractionMode.Normal;
    public EObjectInteractionMode getLocalMode() {
        return localMode;
    }
    public void setMode(EObjectInteractionMode mode){
        this.localMode = mode;
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

    public void getAnimationData(ClientWorldManager.AnimationData animationData){

    }

    @FunctionalInterface
    public interface GameObjectSpawner<T extends GameObject>{
        T spawn(Vector2 position, float rotation, Server server, GameObjectConfig config);
    }
    public static class ConnectionEdge{
        public final Vector2 offset;
        public final ConnectionEdgeType type;
        public final String bodyName;
        public final int priority;
        public ConnectionEdge(Vector2 offset, ConnectionEdgeType type) {
            this(offset, type, "base");
        }
        public ConnectionEdge(Vector2 offset, ConnectionEdgeType type, String bodyName) {
            this(offset, type, bodyName, 0);
        }
        public ConnectionEdge(Vector2 offset, ConnectionEdgeType type, String bodyName, int priority) {
            this.offset = offset;
            this.type = type;
            this.bodyName = bodyName;
            this.priority = priority;
        }
    }
    public enum ConnectionEdgeType{
        Normal,
        Internal,
        Connector,
        WheelConnector;
        public static boolean connects(ConnectionEdgeType first, ConnectionEdgeType second){
            return (first == Normal && second == Normal) || (first == Internal && second == Internal) || (first == Connector && (second == Normal || second == WheelConnector)) || ((first == Normal || first == WheelConnector) && second == Connector);
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
            return gameObject.getBody(connectionEdge.bodyName).getWorldPoint(connectionEdge.offset);
        }
        public boolean collides(GameObjectConnectionEdge other){
            return this.getPosition().dst(other.getPosition()) < 0.2 && ConnectionEdgeType.connects(this.connectionEdge.type, other.connectionEdge.type);
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
    public static class GearConnectionData{
        public final GameObject other;
        public final int thisRatio;
        public final int otherRatio;
        public final Joint joint;
        public GearConnectionData(GameObject other, int thisRatio, int otherRatio, Joint joint) {
            this.other = other;
            this.thisRatio = thisRatio;
            this.otherRatio = otherRatio;
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
    public static class GameObjectConfig{
        public static GameObjectConfig DEFAULT = new GameObjectConfig(Material.Wood, 1);
        public final Material material;
        public final float size;
        public GameObjectConfig(Material material, float size) {
            this.material = material;
            this.size = size;
        }
        public GameObjectConfig(DataInputStream stream) throws IOException {
            this.material = Material.byId(stream.readByte());
            this.size = stream.readFloat();
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeByte(material.id);
            stream.writeFloat(size);
        }
    }
}
