package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class RotatorGameObject extends GameObject {
    private final RevoluteJoint motor;
    public RotatorGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.set(new Vector2[]{new Vector2(0, 0), new Vector2(1, -1), new Vector2(-1, -1)});
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "rotator_join", base);


        Body wheelBody = server.physics.createBody(bodyDef);
        FixtureDef wheelFixtureDef = new FixtureDef();
        PolygonShape wheelShape = new PolygonShape();
        wheelShape.set(new Vector2[]{new Vector2(0, -1), new Vector2(1, 0), new Vector2(-1, 0)});
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.density = 1F;
        wheelBody.createFixture(wheelFixtureDef);
        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
        revoluteJoint.bodyA = wheelBody;
        revoluteJoint.bodyB = base;
        revoluteJoint.localAnchorA.set(new Vector2(0, -1));
        revoluteJoint.localAnchorB.set(new Vector2(0, 0));
        revoluteJoint.maxMotorTorque = 500;
        revoluteJoint.lowerAngle = (float) (-Math.PI/2);
        revoluteJoint.upperAngle = (float) (Math.PI/2);
        revoluteJoint.enableLimit = true;
        revoluteJoint.enableMotor = true;
        revoluteJoint.motorSpeed = (float) (Math.PI/5f);
        this.motor = (RevoluteJoint) this.server.physics.createJoint(revoluteJoint);
        this.setBody("end", "rotator_end", wheelBody);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 50f);
        items.put(EItemType.Circuit, 10f);
        return items;
    }

    @Override
    public void tick() {
        super.tick();
        float value = Math.max(Math.min(getValueOnInput(0),90),-90);
        motor.setMotorSpeed((float) -(Math.toDegrees(motor.getJointAngle())-value)/10);
    }
    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("angle: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), ConnectionEdgeType.Normal));
        edges.put("rotator", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Normal, "end"));
        return edges;
    }
}
