package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ControllerInput {
    public final int gameObjectId;
    public final int key;
    public final boolean down;
    public ControllerInput(int id, int key, boolean down) {
        this.gameObjectId = id;
        this.key = key;
        this.down = down;
    }
    public ControllerInput(DataInputStream stream) throws IOException {
        this.gameObjectId = stream.readInt();
        this.key = stream.readInt();
        this.down = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(gameObjectId);
        stream.writeInt(key);
        stream.writeBoolean(down);
    }
    public static MessageRegistry.MessageDescriptor<ControllerInput> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ControllerInput.class, ControllerInput::new, ControllerInput::toStream);
    }
}
