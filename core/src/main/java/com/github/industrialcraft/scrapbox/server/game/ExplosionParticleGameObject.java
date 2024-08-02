package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ExplosionParticleGameObject extends GameObject {
    private int ttl;
    private boolean cancelled;
    public float power;
    public ExplosionParticleGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.gravityScale = 0;
        bodyDef.bullet = true;
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "explosion_particle", base);
        this.cancelled = false;
        this.ttl = 4;
        this.power = 1;
    }

    @Override
    public void tick() {
        super.tick();
        if(ttl <= 0){
            remove();
        }
        ttl--;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        return new HashMap<>();
    }

    @Override
    public boolean collidesWith(Body thisBody, Body other) {
        if(cancelled)
            return false;
        GameObject go = (GameObject) other.getUserData();
        if(go != null && (go.isRemoved() || go instanceof ExplosionParticleGameObject))
            return false;
        if(go != null)
            go.getBaseBody().applyLinearImpulse(this.getBaseBody().getLinearVelocity().cpy().scl(1.2f), this.getBaseBody().getWorldCenter(), true);
        if(other.getType() == BodyDef.BodyType.StaticBody){
            System.out.println("here");
            server.terrain.place("", thisBody.getWorldCenter(), power/Math.max(3-ttl, 1), false);
        }
        this.cancelled = true;
        return true;
    }
    @Override
    public String getType() {
        return "explosion_particle";
    }
}
