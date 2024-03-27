package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TakeObjectResponse {
    public final int id;
    public final Vector2 offset;
    public TakeObjectResponse(int id, Vector2 offset) {
        this.id = id;
        this.offset = offset;
    }
    public TakeObjectResponse(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.offset = new Vector2(stream.readFloat(), stream.readFloat());
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeFloat(offset.x);
        stream.writeFloat(offset.y);
    }
    public static MessageRegistry.MessageDescriptor<TakeObjectResponse> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(TakeObjectResponse.class, TakeObjectResponse::new, TakeObjectResponse::toStream);
    }
}
