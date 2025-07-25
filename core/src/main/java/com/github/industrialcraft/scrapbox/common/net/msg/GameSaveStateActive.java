package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GameSaveStateActive {
    public final boolean active;
    public GameSaveStateActive(boolean active){
        this.active = active;
    }
    public GameSaveStateActive(DataInputStream stream) throws IOException {
        this.active = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeBoolean(active);
    }
    public static MessageRegistry.MessageDescriptor<GameSaveStateActive> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(GameSaveStateActive.class, GameSaveStateActive::new, GameSaveStateActive::toStream);
    }
}
