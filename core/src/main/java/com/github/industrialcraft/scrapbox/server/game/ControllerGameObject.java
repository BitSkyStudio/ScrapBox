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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class ControllerGameObject extends GameObject {
    public final boolean[] inputs;
    private final ControllerButtonData[] buttonData;
    public ControllerGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
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
        this.setBody("base", "controller", base);

        this.inputs = new boolean[10];
        this.buttonData = new ControllerButtonData[10];
        for(int i = 0;i < 10;i++){
            inputs[i] = false;
            buttonData[i] = new ControllerButtonData();
        }
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
    }
    public void input(int key, boolean down){
        if(buttonData[key].keep){
            if(down){
                inputs[key] = !inputs[key];
            }
        } else {
            inputs[key] = down;
        }
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        for(int i = 0;i < 10;i++){
            buttonData[i] = new ControllerButtonData(stream);
        }
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        for(ControllerButtonData buttonData : this.buttonData){
            buttonData.toStream(stream);
        }
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        ArrayList<String> holdSelection = new ArrayList<>();
        holdSelection.add("hold");
        holdSelection.add("keep");

        for(int i = 0;i < 10;i++){
            ArrayList<EditorUIElement> row = new ArrayList<>();
            int j = i+1;
            row.add(new EditorUILabel(j+":"));
            row.add(new EditorUIDropDown("hold"+j, holdSelection, buttonData[i].keep?1:0));
            row.add(new EditorUIInputBox("low"+j, buttonData[i].low+""));
            row.add(new EditorUIInputBox("high"+j, buttonData[i].high+""));
            row.add(new EditorUILink(i, false, 0f, isInputFilled(0), false));
            rows.add(new EditorUIRow(row));
        }
        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value) {
        super.handleEditorUIInput(elementId, value);
        if(elementId.startsWith("hold")){
            int i = Integer.parseInt(elementId.replace("hold", ""))-1;
            buttonData[i].keep = !buttonData[i].keep;
        }
        try{
            if(elementId.startsWith("low")){
                int i = Integer.parseInt(elementId.replace("low", ""))-1;
                buttonData[i].low = Float.parseFloat(value);
            }
            if(elementId.startsWith("high")){
                int i = Integer.parseInt(elementId.replace("high", ""))-1;
                buttonData[i].high = Float.parseFloat(value);
            }
        } catch (Exception e){}
    }

    @Override
    public float getValueOnOutput(int id) {
        return inputs[id]?buttonData[id].high:buttonData[id].low;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }

    public static class ControllerButtonData{
        public boolean keep;
        private float low;
        private float high;
        public ControllerButtonData() {
            this.keep = false;
            this.low = 0;
            this.high = 1;
        }
        public ControllerButtonData(DataInputStream stream) throws IOException {
            this.keep = stream.readBoolean();
            this.low = stream.readFloat();
            this.high = stream.readFloat();
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeBoolean(keep);
            stream.writeFloat(low);
            stream.writeFloat(high);
        }
    }
}
