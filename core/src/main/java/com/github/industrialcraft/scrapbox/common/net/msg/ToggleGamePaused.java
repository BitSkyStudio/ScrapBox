package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ToggleGamePaused {
    public ToggleGamePaused(){}
    public ToggleGamePaused(DataInputStream stream) throws IOException {

    }
    public void toStream(DataOutputStream stream) throws IOException {

    }
    public static MessageRegistry.MessageDescriptor<ToggleGamePaused> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ToggleGamePaused.class, ToggleGamePaused::new, ToggleGamePaused::toStream);
    }
}
