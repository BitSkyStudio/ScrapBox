package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;

public class SpringGameObject extends GameObject {
    private final PrismaticJoint prismatic;
    private final DistanceJoint distanceJoint;
    public SpringGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.setAsBox(0.125f, 0.5f);
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "spring_start", base);


        Body endBody = server.physics.createBody(bodyDef);
        FixtureDef endFixtureDef = new FixtureDef();
        PolygonShape endShape = new PolygonShape();
        endShape.setAsBox(0.125f, 0.5f);
        endFixtureDef.shape = endShape;
        endFixtureDef.density = 1F;
        endBody.createFixture(endFixtureDef);

        PrismaticJointDef prismaticJoint = new PrismaticJointDef();
        prismaticJoint.bodyA = endBody;
        prismaticJoint.bodyB = base;
        prismaticJoint.localAnchorA.set(new Vector2(0, 0));
        prismaticJoint.localAnchorB.set(new Vector2(0, 0));
        prismaticJoint.localAxisA.set(-1, 0);
        prismaticJoint.referenceAngle = (float) (endBody.getAngle() - base.getAngle() - Math.PI);
        this.prismatic = (PrismaticJoint) this.server.physics.createJoint(prismaticJoint);

        DistanceJointDef distanceJointDef = new DistanceJointDef();
        distanceJointDef.bodyA = endBody;
        distanceJointDef.bodyB = base;
        distanceJointDef.localAnchorA.setZero();
        distanceJointDef.localAnchorB.setZero();
        distanceJointDef.length = 2-0.125f*4f;
        distanceJointDef.dampingRatio = 2f;
        distanceJointDef.frequencyHz = 10f;
        this.distanceJoint = (DistanceJoint) this.server.physics.createJoint(distanceJointDef);

        this.setBody("second", "spring_end", endBody, true);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 30f);
        return items;
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        super.getAnimationData(animationData);
        if(prismatic != null)
            animationData.addNumber("length", prismatic.getJointTranslation());
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeFloat(prismatic.getJointTranslation());
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        Vector2 offset = new Vector2(-stream.readFloat()-0.1f, 0);
        offset.rotateRad((float) (getBaseBody().getAngle()));
        getBody("second").setTransform(getBaseBody().getPosition().add(offset), (float) (getBaseBody().getAngle()+Math.PI));
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(-0.125f, 0), ConnectionEdgeType.Normal, "base", 2));
        edges.put("end", new ConnectionEdge(new Vector2(-0.125f, 0), ConnectionEdgeType.Normal, "second", 1));
        return edges;
    }
}
