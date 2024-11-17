package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class MotorGameObject extends GameObject implements IGearJoinable {
    private final Body rotatingBody;
    private final RevoluteJoint motorJoint;
    public MotorGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "motor", base);

        this.rotatingBody = server.physics.createBody(bodyDef);
        fixtureDef.isSensor = true;
        rotatingBody.createFixture(fixtureDef);

        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.maxMotorTorque = 1000;
        revoluteJointDef.bodyA = base;
        revoluteJointDef.bodyB = rotatingBody;
        revoluteJointDef.enableMotor = true;
        this.motorJoint = (RevoluteJoint) server.physics.createJoint(revoluteJointDef);
    }

    @Override
    public void destroy() {
        super.destroy();
        server.physics.destroyJoint(motorJoint);
        server.physics.destroyBody(rotatingBody);
    }

    @Override
    public void tick() {
        super.tick();
        float value = Math.max(Math.min(getValueOnInput(0),1),-1);
        if(value != 0){
            motorJoint.enableMotor(true);
            motorJoint.setMotorSpeed(value*12*1.6f);
        } else {
            motorJoint.enableMotor(false);
        }
    }

    @Override
    public Joint getGearJoint() {
        return motorJoint;
    }
    @Override
    public Body getGearJointBody() {
        return rotatingBody;
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 50f);
        items.put(EItemType.Transmission, 50f);
        return items;
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("value: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));

        return rows;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }
}
