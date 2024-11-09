package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.IPairObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class BalloonGameObject extends GameObject implements IPairObject {
    public GameObject other;
    public RopeJoint joint;
    public BalloonGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = 4;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        CircleShape baseShape = new CircleShape();
        baseShape.setRadius(0.8f);
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "balloon", base);
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        super.getAnimationData(animationData);
        if(joint != null)
            animationData.addNumber("length", joint.getMaxLength());
        if(other != null)
            animationData.addString("other", ""+other.getId());
        animationData.addNumber("offsetX", getPairJointOffset().x);
        animationData.addNumber("offsetY", getPairJointOffset().y);
    }

    @Override
    public void tick() {
        super.tick();
        getBaseBody().applyForce(new Vector2(0, 1000), getBaseBody().getWorldPoint(new Vector2(0, 1)), true);

        if(other == null){
            RopeGameObject other = server.spawnGameObject(getBaseBody().getPosition(), getBaseBody().getAngle(), RopeGameObject::new, UUID.randomUUID(), GameObjectConfig.DEFAULT);
            other.setMode(EObjectInteractionMode.Ghost);
            other.other = this;
            this.other = other;
            RopeJointDef jointDef = new RopeJointDef();
            jointDef.maxLength = 2f;
            jointDef.bodyA = this.getBaseBody();
            jointDef.bodyB = other.getBaseBody();
            jointDef.localAnchorA.set(this.getPairJointOffset());
            jointDef.localAnchorB.set(other.getPairJointOffset());
            jointDef.collideConnected = true;
            RopeJoint joint = (RopeJoint) server.physics.createJoint(jointDef);
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
                RopeJointDef jointDef = new RopeJointDef();
                jointDef.maxLength = stream.readFloat();
                jointDef.bodyA = this.getBaseBody();
                jointDef.bodyB = other.getBaseBody();
                jointDef.localAnchorA.set(this.getPairJointOffset());
                jointDef.localAnchorB.set(((IPairObject)other).getPairJointOffset());
                jointDef.collideConnected = true;
                RopeJoint joint = (RopeJoint) server.physics.createJoint(jointDef);
                this.joint = joint;
                ((RopeGameObject)other).joint = joint;
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
        stream.writeFloat(joint==null?2:joint.getMaxLength());
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        return new HashMap<>();
    }

    @Override
    public String getType() {
        return "balloon";
    }

    @Override
    public void changeDistance(float by) {
        if(this.joint != null)
            joint.setMaxLength(Math.max(Math.min(joint.getMaxLength()-by, 10), 1));
    }
    @Override
    public GameObject getOther() {
        return other;
    }

    @Override
    public Vector2 getPairJointOffset() {
        return new Vector2(0, -0.75f);
    }
}
