package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChangeObjectHealth {
    public final int gameObjectId;
    public final float health;
    public ChangeObjectHealth(int gameObjectId, float health) {
        this.gameObjectId = gameObjectId;
        this.health = health;
    }
    public ChangeObjectHealth(DataInputStream stream) throws IOException {
        this.gameObjectId = stream.readInt();
        this.health = stream.readFloat();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(gameObjectId);
        stream.writeFloat(health);
    }
    public static MessageRegistry.MessageDescriptor<ChangeObjectHealth> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ChangeObjectHealth.class, ChangeObjectHealth::new, ChangeObjectHealth::toStream);
    }
}
