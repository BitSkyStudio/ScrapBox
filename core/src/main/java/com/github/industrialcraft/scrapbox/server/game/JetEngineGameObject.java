package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
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

public class JetEngineGameObject extends GameObject {
    public float speed;
    public JetEngineGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = 3;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1-1/8f, 1f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "jet_engine", base);

        this.speed = 0;
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 50f);
        items.put(EItemType.Explosive, 50f);
        items.put(EItemType.Circuit, 10f);
        return items;
    }
    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        super.getAnimationData(animationData);
        animationData.addNumber("speed", this.speed);
    }
    @Override
    public void tick() {
        super.tick();
        this.speed = Math.max(Math.min(getValueOnInput(0),1),-1);
        float angle = getBaseBody().getAngle();
        getBaseBody().applyForceToCenter(new Vector2((float) -Math.sin(angle), (float) Math.cos(angle)).scl(6000*speed), true);
        if(speed != 0) {
            for (int i = 0; i < 3; i++) {
                FireParticleGameObject fireParticle = server.spawnGameObject(getBaseBody().getWorldPoint(new Vector2(0, -1)).cpy(), 0, FireParticleGameObject::new, null, GameObjectConfig.DEFAULT);
                fireParticle.parent = this;
                fireParticle.ttl = (int) (8 + Math.random() * 3);
                float speed = 15f / 10f;
                float angle2 = (float) (-getBaseBody().getAngle() + Math.PI + ((Math.random() * 2 - 1) * Math.PI / 12));
                fireParticle.getBaseBody().applyLinearImpulse(new Vector2((float) (Math.sin(angle2) * speed), (float) (Math.cos(angle2) * speed)), fireParticle.getBaseBody().getPosition(), true);
            }
        }
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("speed: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, 1f), ConnectionEdgeType.Normal));
        return edges;
    }
}
