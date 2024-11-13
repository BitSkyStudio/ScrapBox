package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PistonGameObject extends GameObject {
    private final PrismaticJoint motor;
    public PistonGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.setAsBox(FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE);
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "piston_box", base);


        Body endBody = server.physics.createBody(bodyDef);
        FixtureDef endFixtureDef = new FixtureDef();
        PolygonShape endShape = new PolygonShape();
        endShape.setAsBox(1, 0.125f);
        endFixtureDef.shape = endShape;
        endFixtureDef.density = 1F;
        endBody.createFixture(endFixtureDef);
        PrismaticJointDef prismaticJoint = new PrismaticJointDef();
        prismaticJoint.bodyA = endBody;
        prismaticJoint.bodyB = base;
        prismaticJoint.localAnchorA.set(new Vector2(0, -1));
        prismaticJoint.localAnchorB.set(new Vector2(0, 0));
        prismaticJoint.localAxisA.set(0, -1);
        prismaticJoint.maxMotorForce = 1000;
        prismaticJoint.lowerTranslation = 0;
        prismaticJoint.upperTranslation = 5f;
        prismaticJoint.enableLimit = true;
        prismaticJoint.enableMotor = true;
        this.motor = (PrismaticJoint) this.server.physics.createJoint(prismaticJoint);
        this.setBody("end", "piston_end", endBody);
    }

    @Override
    public String getGearJointBody() {
        return "end";
    }

    @Override
    public Joint getGearJoint() {
        return motor;
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        super.getAnimationData(animationData);
        if(motor != null)
            animationData.addNumber("length", motor.getJointTranslation());
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeFloat(motor.getJointTranslation());
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        Vector2 offset = new Vector2(0, 1 + stream.readFloat());
        offset.rotateRad(getBaseBody().getAngle());
        getBody("end").setTransform(getBaseBody().getPosition().add(offset), getBaseBody().getAngle());
    }

    @Override
    public void tick() {
        super.tick();
        float value = Math.max(Math.min(getValueOnInput(0),2.5f),0)*2;
        motor.setMotorSpeed(-((motor.getJointTranslation())-value)*5);
        motor.enableMotor(gearConnections.isEmpty());
    }
    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("length: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }

    @Override
    public boolean collidesWith(Fixture thisFixture, Fixture other) {
        if(connections.containsKey("center") && other.getBody().getUserData() == connections.get("center").other)
            return false;
        return super.collidesWith(thisFixture, other);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal, "base", 2));
        edges.put("end", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Normal, "end", 1));
        return edges;
    }

    @Override
    public String getType() {
        return "piston";
    }
}
