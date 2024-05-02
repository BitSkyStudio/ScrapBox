package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class MathUnitGameObject extends GameObject {
    public static final String[] OPERATION_LIST;
    static{
        OPERATION_LIST = new String[]{"+","-","*","/"};
    }

    private final int[] operations;
    public MathUnitGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        this.operations = new int[10];
        Arrays.fill(operations, 0);

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
        this.setBody("base", "math_unit", base);
    }
    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        for(int i = 0;i < operations.length;i++)
            operations[i] = stream.readInt();
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        for (int operation : operations) stream.writeInt(operation);
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        ArrayList<String> calibrationSelection = new ArrayList<>(List.of(OPERATION_LIST));
        for(int i = 0;i < operations.length;i++){
            ArrayList<EditorUIElement> row = new ArrayList<>();
            row.add(new EditorUILink(i*2, true, defaultValues.getOrDefault(i*2, 0f), isInputFilled(i*2)));
            row.add(new EditorUIDropDown("op"+i, calibrationSelection, operations[i]));
            row.add(new EditorUILink(i*2 + 1, true, defaultValues.getOrDefault(i*2+1, 0f), isInputFilled(i*2 + 1)));
            row.add(new EditorUILabel("="));
            row.add(new EditorUILink(i, false, 0f, false));
            rows.add(new EditorUIRow(row));
        }
        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value) {
        super.handleEditorUIInput(elementId, value);
        if(elementId.startsWith("op")){
            int i = Integer.parseInt(elementId.replace("op", ""));
            operations[i] = Integer.parseInt(value);
        }
    }

    @Override
    public float getValueOnOutput(int id) {
        float first = getValueOnInput(id*2);
        float second = getValueOnInput(id*2+1);
        if(operations[id] == 0){
            return first+second;
        } else if(operations[id] == 1){
            return first-second;
        } else if(operations[id] == 2){
            return first*second;
        } else if(operations[id] == 3){
            return first/second;
        }
        throw new IllegalArgumentException("invalid operation");
    }

    @Override
    public Joint createJoint(String thisName, GameObject other, String otherName) {
        float rotationOffset = (float) (Math.round((other.getBaseBody().getAngle()-this.getBaseBody().getAngle())/HALF_PI)*HALF_PI);
        Transform transform = other.getBaseBody().getTransform();
        this.getBaseBody().setTransform(transform.getPosition(), transform.getRotation()-rotationOffset);
        WeldJointDef joint = new WeldJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.getBaseBody();
        joint.localAnchorA.set(new Vector2(0, 0));
        joint.localAnchorB.set(new Vector2(0, 0));
        joint.referenceAngle = rotationOffset;
        return this.server.physics.createJoint(joint);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), true));
        return edges;
    }

    @Override
    public String getType() {
        return "math_unit";
    }
}
