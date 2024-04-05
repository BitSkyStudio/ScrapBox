package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlaceTerrain {
    public final String type;
    public final Vector2 position;
    public final float radius;
    public PlaceTerrain(String type, Vector2 position, float radius) {
        this.type = type;
        this.position = position;
        this.radius = radius;
    }
    public PlaceTerrain(DataInputStream stream) throws IOException {
        this.type = stream.readUTF();
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
        this.radius = stream.readFloat();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(type);
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(radius);
    }
    public static MessageRegistry.MessageDescriptor<PlaceTerrain> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PlaceTerrain.class, PlaceTerrain::new, PlaceTerrain::toStream);
    }
}
