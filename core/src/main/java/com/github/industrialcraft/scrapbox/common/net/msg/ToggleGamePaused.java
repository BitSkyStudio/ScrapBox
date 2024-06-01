package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ToggleGamePaused {
    public final boolean step;
    public ToggleGamePaused(boolean step){
        this.step = step;
    }
    public ToggleGamePaused(DataInputStream stream) throws IOException {
        this.step = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeBoolean(step);
    }
    public static MessageRegistry.MessageDescriptor<ToggleGamePaused> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ToggleGamePaused.class, ToggleGamePaused::new, ToggleGamePaused::toStream);
    }
}
