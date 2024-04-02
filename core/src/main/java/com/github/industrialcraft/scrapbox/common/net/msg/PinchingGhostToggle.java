package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PinchingGhostToggle {
    public PinchingGhostToggle() {

    }
    public PinchingGhostToggle(DataInputStream stream) throws IOException {

    }
    public void toStream(DataOutputStream stream) throws IOException {

    }
    public static MessageRegistry.MessageDescriptor<PinchingGhostToggle> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PinchingGhostToggle.class, PinchingGhostToggle::new, PinchingGhostToggle::toStream);
    }
}
