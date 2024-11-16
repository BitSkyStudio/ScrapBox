package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;

public class StickGameObject extends GameObject implements IPairObject {
    public GameObject other;
    public DistanceJoint joint;
    public StickGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);
        fixtureDef.shape = shape;
        fixtureDef.density = 5F;
        base.createFixture(fixtureDef);
        this.setBody("base", "stick_connector", base);

        this.other = null;
        this.joint = null;
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 50f);
        return items;
    }
    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        if(other != null)
            animationData.addString("other", ""+other.getId());
    }

    @Override
    public void tick() {
        super.tick();
        if(other == null){
            StickGameObject other = server.spawnGameObject(getBaseBody().getPosition(), getBaseBody().getAngle(), StickGameObject::new, UUID.randomUUID(), this.config);
            other.setMode(EObjectInteractionMode.Ghost);
            other.other = this;
            this.other = other;
            DistanceJointDef jointDef = new DistanceJointDef();
            jointDef.length = 2f;
            jointDef.bodyA = this.getBaseBody();
            jointDef.bodyB = other.getBaseBody();
            jointDef.localAnchorA.set(this.getPairJointOffset());
            jointDef.localAnchorB.set(other.getPairJointOffset());
            DistanceJoint joint = (DistanceJoint) server.physics.createJoint(jointDef);
            this.joint = joint;
            other.joint = joint;
        } else if(other.isRemoved()){
            this.remove();
        }
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        if(stream.readBoolean()) {
            this.other = server.getGameObjectByUUID(new UUID(stream.readLong(), stream.readLong()));
            if(other != null && this.getId() < other.getId()){
                DistanceJointDef jointDef = new DistanceJointDef();
                jointDef.length = stream.readFloat();
                jointDef.bodyA = this.getBaseBody();
                jointDef.bodyB = other.getBaseBody();
                jointDef.localAnchorA.set(this.getPairJointOffset());
                jointDef.localAnchorB.set(((IPairObject)other).getPairJointOffset());
                DistanceJoint joint = (DistanceJoint) server.physics.createJoint(jointDef);
                this.joint = joint;
                ((StickGameObject)other).joint = joint;
            }
        }
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeBoolean(other != null);
        if(other != null) {
            stream.writeLong(other.uuid.getMostSignificantBits());
            stream.writeLong(other.uuid.getLeastSignificantBits());
        }
        stream.writeFloat(joint==null?2:joint.getLength());
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Connector));
        return edges;
    }

    @Override
    public String getType() {
        return "stick";
    }

    @Override
    public void changeDistance(float by) {
        if(this.joint != null)
            joint.setLength(Math.max(Math.min(joint.getLength()-by, 10), 1));
    }
    @Override
    public GameObject getOther() {
        return other;
    }

    @Override
    public Vector2 getPairJointOffset() {
        return Vector2.Zero;
    }
}
