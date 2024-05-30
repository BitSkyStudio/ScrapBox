package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class MathUnitGameObject extends GameObject {
    public static final MathOperation[] OPERATION_LIST;
    static{
        OPERATION_LIST = new MathOperation[]{
            new MathOperation("+", (first, second) -> first+second),
            new MathOperation("-", (first, second) -> first-second),
            new MathOperation("*", (first, second) -> first*second),
            new MathOperation("/", (first, second) -> first/second)
        };
    }

    private ArrayList<Integer> operations;
    private ArrayList<Float> outputs;
    public MathUnitGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        this.operations = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.operations.add(0);

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
        ArrayList<Float> newOutputs = new ArrayList<>();
        for(int i = 0;i < operations.size();i++){
            float first = getValueOnInput(i*2);
            float second = getValueOnInput(i*2+1);
            newOutputs.add(OPERATION_LIST[operations.get(i)].function.apply(first, second));
        }
        this.outputs = newOutputs;
        super.tick();
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        operations.clear();
        int count = stream.readInt();
        for(int i = 0;i < count;i++)
            operations.add(stream.readInt());
        int outputsCount = stream.readInt();
        for(int i = 0;i < outputsCount;i++)
            outputs.add(stream.readFloat());
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeInt(operations.size());
        for (int operation : operations) stream.writeInt(operation);
        stream.writeInt(outputs.size());
        for (float output : outputs) stream.writeFloat(output);
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        ArrayList<String> calibrationSelection = Arrays.stream(OPERATION_LIST).map(mathOperation -> mathOperation.text).collect(Collectors.toCollection(ArrayList::new));
        for(int i = 0;i < operations.size();i++){
            ArrayList<EditorUIElement> row = new ArrayList<>();
            row.add(new EditorUILink(i*2, true, defaultValues.getOrDefault(i*2, 0f), isInputFilled(i*2)));
            row.add(new EditorUIDropDown("op"+i, calibrationSelection, operations.get(i)));
            row.add(new EditorUILink(i*2 + 1, true, defaultValues.getOrDefault(i*2+1, 0f), isInputFilled(i*2 + 1)));
            row.add(new EditorUILabel("="));
            row.add(new EditorUILink(i, false, 0f, false));
            row.add(new EditorUIButton("X", "close"+i));
            rows.add(new EditorUIRow(row));
        }
        rows.add(new EditorUIRow(new ArrayList<>(List.of(new EditorUIButton("New", "new")))));
        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value) {
        super.handleEditorUIInput(elementId, value);
        if(elementId.startsWith("op")){
            int i = Integer.parseInt(elementId.replace("op", ""));
            operations.set(i, Integer.parseInt(value));
        }
        if(elementId.startsWith("close")){
            int i = Integer.parseInt(elementId.replace("close", ""));
            operations.remove(i);
            for(int j = i;j < operations.size();j++){
                valueConnections.put(j*2, valueConnections.get((j+1)*2));
                valueConnections.put(j*2+1, valueConnections.get((j+1)*2+1));
            }
            destroyValueConnection(operations.size()*2);
            destroyValueConnection(operations.size()*2+1);
        }
        if(elementId.equals("new")){
            operations.add(0);
        }
    }

    @Override
    public float getValueOnOutput(int id) {
        if(id >= this.outputs.size()){
            return 0;
        }
        return this.outputs.get(id);
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

    public static class MathOperation{
        public final String text;
        public final BiFunction<Float,Float,Float> function;
        public MathOperation(String text, BiFunction<Float, Float, Float> function) {
            this.text = text;
            this.function = function;
        }
    }
}
