package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.common.Material;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class StabilizerGameObject extends GameObject {
    public StabilizerGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 0.6f);
        fixtureDef.shape = shape;
        fixtureDef.density = config.<Material>getProperty(GameObjectConfig.Property.OMaterial).density;
        base.createFixture(fixtureDef);
        this.setBody("base", "stabilizer", base);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Wood, 50f);
        return items;
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        super.getAnimationData(animationData);
        animationData.addNumber("angle", getValueOnInput(0));
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
    public void tick() {
        super.tick();
        float value = (float)Math.toRadians(getValueOnInput(0)) - getBaseBody().getAngle();
        value %= Math.PI * 2;
        value += Math.PI * 2;
        value %= Math.PI * 2;
        float distance = (float) Math.min(Math.abs(value), Math.abs(Math.PI-value));
        float sign = value>Math.PI?1:-1;
        getBaseBody().applyLinearImpulse(getBaseBody().getLinearVelocity().rotate90((int) sign).scl(0.1f * Math.min(distance, 1)), getBaseBody().getWorldPoint(Vector2.Zero), true);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("right", new ConnectionEdge(new Vector2(1, 0), ConnectionEdgeType.Normal));
        return edges;
    }
}
