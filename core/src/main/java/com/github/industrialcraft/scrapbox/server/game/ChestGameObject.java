package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ChestGameObject extends GameObject {
    public EnumMap<EItemType, Float> inventory;
    public ChestGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
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
        this.setBody("base", "chest", base);

        this.inventory = new EnumMap<>(EItemType.class);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Wood, 50f);
        return items;
    }

    @Override
    public float getValueOnOutput(int id) {
        return inventory.getOrDefault(EItemType.byId((byte) id), 0f);
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeInt(inventory.size());
        for(Map.Entry<EItemType, Float> entry : inventory.entrySet()){
            stream.writeByte(entry.getKey().id);
            stream.writeFloat(entry.getValue());
        }
    }
    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        this.inventory.clear();
        int size = stream.readInt();
        for(int i = 0;i < size;i++){
            this.inventory.put(EItemType.byId(stream.readByte()), stream.readFloat());
        }
    }
    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        EditorUIRow row = new EditorUIRow(new ArrayList<>());
        row.elements.add(new EditorUIInventory(inventory));
        rows.add(row);
        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value, Player player) {
        super.handleEditorUIInput(elementId, value, player);
        if(elementId.equals("inventory_add")){
            EItemType itemType = EItemType.byId(Byte.parseByte(value));
            float itemTransfer = player.getTeam().infiniteItems?1:Math.min(1, player.getTeam().getItemCount(itemType));
            player.getTeam().removeItems(itemType, itemTransfer);
            inventory.put(itemType, inventory.getOrDefault(itemType, 0f)+itemTransfer);
        }
        if(elementId.equals("inventory_sub")){
            EItemType itemType = EItemType.byId(Byte.parseByte(value));
            float itemTransfer = Math.min(1, inventory.getOrDefault(itemType, 0f));
            player.getTeam().addItems(itemType, itemTransfer);
            inventory.put(itemType, inventory.getOrDefault(itemType, 0f)-itemTransfer);
        }
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }
}
