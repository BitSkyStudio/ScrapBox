package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ToggleSaveState {
    public final boolean reset;
    public ToggleSaveState(boolean reset){
        this.reset = reset;
    }
    public ToggleSaveState(DataInputStream stream) throws IOException {
        this.reset = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeBoolean(this.reset);
    }
    public static MessageRegistry.MessageDescriptor<ToggleSaveState> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ToggleSaveState.class, ToggleSaveState::new, ToggleSaveState::toStream);
    }
}
