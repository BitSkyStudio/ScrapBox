package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CreateGearConnection {
    public final int objectA;
    public final int gearRatioA;
    public final int objectB;
    public final int gearRatioB;
    public CreateGearConnection(int objectA, int gearRatioA, int objectB, int gearRatioB) {
        this.objectA = objectA;
        this.gearRatioA = gearRatioA;
        this.objectB = objectB;
        this.gearRatioB = gearRatioB;
    }
    public CreateGearConnection(DataInputStream stream) throws IOException {
        this.objectA = stream.readInt();
        this.gearRatioA = stream.readInt();
        this.objectB = stream.readInt();
        this.gearRatioB = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(objectA);
        stream.writeInt(gearRatioA);
        stream.writeInt(objectB);
        stream.writeInt(gearRatioB);
    }
    public static MessageRegistry.MessageDescriptor<CreateGearConnection> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(CreateGearConnection.class, CreateGearConnection::new, CreateGearConnection::toStream);
    }
}
