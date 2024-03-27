package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlaceTerrain {
    public final Vector2 position;
    public final float radius;
    public PlaceTerrain(Vector2 position, float radius) {
        this.position = position;
        this.radius = radius;
    }
    public PlaceTerrain(DataInputStream stream) throws IOException {
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
        this.radius = stream.readFloat();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(radius);
    }
    public static MessageRegistry.MessageDescriptor<PlaceTerrain> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PlaceTerrain.class, PlaceTerrain::new, PlaceTerrain::toStream);
    }
}
