package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GamePausedState {
    public final boolean paused;
    public GamePausedState(boolean paused){
        this.paused = paused;
    }
    public GamePausedState(DataInputStream stream) throws IOException {
        this.paused = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeBoolean(paused);
    }
    public static MessageRegistry.MessageDescriptor<GamePausedState> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(GamePausedState.class, GamePausedState::new, GamePausedState::toStream);
    }
}
