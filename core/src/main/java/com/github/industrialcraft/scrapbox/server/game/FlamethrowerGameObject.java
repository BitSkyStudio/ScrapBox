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
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class FlamethrowerGameObject extends GameObject {
    public FlamethrowerGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.6f, 1);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "flamethrower", base);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 50f);
        items.put(EItemType.Explosive, 30f);
        return items;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), ConnectionEdgeType.Normal));
        return edges;
    }

    @Override
    public void tick() {
        super.tick();
        boolean newInput = getValueOnInput(0) != 0;
        if(newInput && getLocalMode() != EObjectInteractionMode.Ghost) {
            float explosiveCost = 1f/50f;
            if(vehicle.countItem(EItemType.Explosive) >= explosiveCost) {
                vehicle.removeItem(EItemType.Explosive, explosiveCost);
                for (int i = 0; i < 3; i++) {
                    FireParticleGameObject fireParticle = server.spawnGameObject(getBaseBody().getWorldPoint(Vector2.Y).cpy(), 0, FireParticleGameObject::new, null, GameObjectConfig.DEFAULT);
                    fireParticle.parent = this;
                    fireParticle.ttl = (int) (8 + Math.random() * 3);
                    fireParticle.damage = 1;
                    float speed = 15f / 10f;
                    float angle = (float) (-getBaseBody().getAngle() + ((Math.random() * 2 - 1) * Math.PI / 12));
                    fireParticle.getBaseBody().applyLinearImpulse(new Vector2((float) (Math.sin(angle) * speed), (float) (Math.cos(angle) * speed)), fireParticle.getBaseBody().getPosition(), true);
                }
            }
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
}
