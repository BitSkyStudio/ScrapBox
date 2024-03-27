package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MouseMoved {
    public final Vector2 position;
    public MouseMoved(Vector2 position) {
        this.position = position;
    }
    public MouseMoved(DataInputStream stream) throws IOException {
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
    }
    public static MessageRegistry.MessageDescriptor<MouseMoved> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(MouseMoved.class, MouseMoved::new, MouseMoved::toStream);
    }
}
