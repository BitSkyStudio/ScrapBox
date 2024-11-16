package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.EItemType;
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
            new MathOperation("+", (first, second) -> first+second, false),
            new MathOperation("-", (first, second) -> first-second, false),
            new MathOperation("*", (first, second) -> first*second, false),
            new MathOperation("/", (first, second) -> first/second, false),
            new MathOperation("%", (first, second) -> first%second, false),
            new MathOperation("pow", (first, second) -> (float) Math.pow(first, second), false),
            new MathOperation("log", (first, second) -> (float) (Math.log(first)/Math.log(second)), false),
            new MathOperation("max", Math::max, false),
            new MathOperation("min", Math::min, false),
            new MathOperation("abs", (first, second) -> Math.abs(first), true),
            new MathOperation("round", (first, second) -> (float) Math.round(first), true),
            new MathOperation("floor", (first, second) -> (float) Math.floor(first), true),
            new MathOperation("ceil", (first, second) -> (float) Math.ceil(first), true),
            new MathOperation("sin", (first, second) -> (float) Math.sin(Math.toRadians(first)), true),
            new MathOperation("cos", (first, second) -> (float) Math.cos(Math.toRadians(first)), true),
            new MathOperation("tan", (first, second) -> (float) Math.tan(Math.toRadians(first)), true),
            new MathOperation("atan2", (first, second) -> (float) Math.atan2(first, second), false),
        };
    }
    private ArrayList<Integer> operations;
    private ArrayList<Float> outputs;
    public MathUnitGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);
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
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 20f);
        items.put(EItemType.Circuit, 50f);
        return items;
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
            row.add(new EditorUILink(i*2, true, defaultValues.getOrDefault(i*2, 0f), isInputFilled(i*2), false));
            row.add(new EditorUIDropDown("op"+i, calibrationSelection, operations.get(i)));
            row.add(new EditorUILink(i*2 + 1, true, defaultValues.getOrDefault(i*2+1, 0f), isInputFilled(i*2 + 1), OPERATION_LIST[operations.get(i)].secondDisabled));
            row.add(new EditorUILabel("="));
            row.add(new EditorUILink(i, false, 0f, false, false));
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
                defaultValues.put(j, defaultValues.getOrDefault(j+1, 0f));
            }
            destroyValueConnection(operations.size()*2);
            destroyValueConnection(operations.size()*2+1);
            defaultValues.remove(operations.size());
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
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }

    public static class MathOperation{
        public final String text;
        public final BiFunction<Float,Float,Float> function;
        public final boolean secondDisabled;
        public MathOperation(String text, BiFunction<Float, Float, Float> function, boolean secondDisabled) {
            this.text = text;
            this.function = function;
            this.secondDisabled = secondDisabled;
        }
    }
}
