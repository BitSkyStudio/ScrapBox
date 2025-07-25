package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ToggleSaveState {
    public ToggleSaveState(){

    }
    public ToggleSaveState(DataInputStream stream) throws IOException {

    }
    public void toStream(DataOutputStream stream) throws IOException {

    }
    public static MessageRegistry.MessageDescriptor<ToggleSaveState> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ToggleSaveState.class, ToggleSaveState::new, ToggleSaveState::toStream);
    }
}
