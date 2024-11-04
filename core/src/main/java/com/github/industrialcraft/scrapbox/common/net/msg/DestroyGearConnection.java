package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DestroyGearConnection {
    public final int objectA;
    public final int objectB;
    public DestroyGearConnection(int objectA, int objectB) {
        this.objectA = objectA;
        this.objectB = objectB;
    }
    public DestroyGearConnection(DataInputStream stream) throws IOException {
        this.objectA = stream.readInt();
        this.objectB = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(objectA);
        stream.writeInt(objectB);
    }
    public static MessageRegistry.MessageDescriptor<DestroyGearConnection> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(DestroyGearConnection.class, DestroyGearConnection::new, DestroyGearConnection::toStream);
    }
}
