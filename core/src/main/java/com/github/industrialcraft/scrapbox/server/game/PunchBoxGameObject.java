package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class PunchBoxGameObject extends GameObject {
    private final PrismaticJoint motor;
    public PunchBoxGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.setAsBox(FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE);
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "puncher_box", base);


        Body puncherBody = server.physics.createBody(bodyDef);
        FixtureDef puncherFixtureDef = new FixtureDef();
        CircleShape puncherShape = new CircleShape();
        puncherShape.setRadius(0.5f);
        puncherFixtureDef.shape = puncherShape;
        puncherFixtureDef.density = 1F;
        Fixture puncherFixture = puncherBody.createFixture(puncherFixtureDef);
        puncherFixture.setUserData("");
        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
        prismaticJointDef.bodyA = puncherBody;
        prismaticJointDef.bodyB = base;
        prismaticJointDef.localAnchorA.set(0, 0);
        prismaticJointDef.localAnchorB.set(0, 0);
        prismaticJointDef.localAxisA.set(0, -1);
        prismaticJointDef.enableLimit = true;
        prismaticJointDef.lowerTranslation = 0;
        prismaticJointDef.upperTranslation = 4;
        prismaticJointDef.maxMotorForce = 1000;
        prismaticJointDef.enableMotor = true;
        this.motor = (PrismaticJoint) this.server.physics.createJoint(prismaticJointDef);
        this.setBody("puncher", "puncher", puncherBody);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Wood, 50f);
        items.put(EItemType.StickyResin, 20f);
        items.put(EItemType.Metal, 40f);
        return items;
    }

    @Override
    public boolean collidesWith(Fixture thisFixture, Fixture other) {
        if(thisFixture.getBody() != motor.getBodyA()){
            return true;
        }
        if(connections.get("center") == null){
            return true;
        }
        return other.getBody().getUserData() != connections.get("center").other;
    }

    @Override
    public void onCollision(Fixture thisFixture, Fixture other) {
        if(thisFixture.getUserData() instanceof String){
            if(motor.getJointSpeed() > 1){
                if(other.getBody().getUserData() instanceof GameObject){
                    GameObject otherObject = (GameObject) other.getBody().getUserData();
                    otherObject.damage(20, EDamageType.Impact);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        float value = Math.max(Math.min(getValueOnInput(0),1),0)-0.5f;
        motor.setMotorSpeed(value*100);
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        if(motor != null)
            animationData.addNumber("animation", motor.getJointTranslation()/4);
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
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }

    @Override
    public String getType() {
        return "puncher";
    }
}
