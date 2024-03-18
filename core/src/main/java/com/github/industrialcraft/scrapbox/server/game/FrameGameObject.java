package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

public class FrameGameObject extends GameObject {
    public FrameGameObject(Vector2 position, Server server) {
        super(position, server);
    }
    @Override
    protected void add_fixtures() {
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);
        fixtureDef.shape = shape;
        fixtureDef.density = 1;
        this.body.createFixture(fixtureDef);
    }
    @Override
    public String get_type() {
        return "frame";
    }
}
