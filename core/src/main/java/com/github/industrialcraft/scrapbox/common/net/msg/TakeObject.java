package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.server.GameObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TakeObject {
    public final String type;
    public final Vector2 position;
    public final Vector2 offset;
    public final GameObject.GameObjectConfig config;
    public TakeObject(String type, Vector2 position, Vector2 offset, GameObject.GameObjectConfig config) {
        this.type = type;
        this.position = position;
        this.offset = offset;
        this.config = config;
    }
    public TakeObject(DataInputStream stream) throws IOException {
        this.type = stream.readUTF();
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
        this.offset = new Vector2(stream.readFloat(), stream.readFloat());
        this.config = new GameObject.GameObjectConfig(stream);
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(type);
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(offset.x);
        stream.writeFloat(offset.y);
        config.toStream(stream);
    }
    public static MessageRegistry.MessageDescriptor<TakeObject> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(TakeObject.class, TakeObject::new, TakeObject::toStream);
    }
}
