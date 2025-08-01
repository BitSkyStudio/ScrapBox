package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.EDamageType;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class TntGameObject extends GameObject {
    public TntGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "tnt", base);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Wood, 10f);
        items.put(EItemType.Explosive, 50f);
        return items;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), ConnectionEdgeType.Normal));
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), ConnectionEdgeType.Normal));
        edges.put("left", new ConnectionEdge(new Vector2(-1, 0), ConnectionEdgeType.Normal));
        edges.put("right", new ConnectionEdge(new Vector2(1, 0), ConnectionEdgeType.Normal));
        return edges;
    }

    @Override
    public void damage(float amount, EDamageType damageType) {
        if(damageType != EDamageType.Wrench)
            explode();
        super.damage(amount, damageType);
    }

    @Override
    public void tick() {
        super.tick();
        if(getValueOnInput(0) != 0 && getLocalMode() != EObjectInteractionMode.Ghost){
            explode();
        }
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("trigger: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }

    public void explode(){
        if(isRemoved())
            return;
        remove();
        server.createExplosion(getBaseBody().getPosition(), 3);
        playSound("explosion", false, 0.7f);
    }
}
