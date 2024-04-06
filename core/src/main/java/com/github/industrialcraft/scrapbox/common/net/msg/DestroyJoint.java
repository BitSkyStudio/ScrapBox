package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DestroyJoint {
    public final int gameObjectId;
    public final String name;
    public DestroyJoint(int gameObjectId, String name) {
        this.gameObjectId = gameObjectId;
        this.name = name;
    }
    public DestroyJoint(DataInputStream stream) throws IOException {
        this.gameObjectId = stream.readInt();
        this.name = stream.readUTF();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(gameObjectId);
        stream.writeUTF(name);
    }
    public static MessageRegistry.MessageDescriptor<DestroyJoint> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(DestroyJoint.class, DestroyJoint::new, DestroyJoint::toStream);
    }
}
