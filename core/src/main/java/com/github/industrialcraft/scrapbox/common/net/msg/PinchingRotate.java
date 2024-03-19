package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class PinchingRotate extends MessageC2S {
    public final float rotation;
    public PinchingRotate(float rotation) {
        this.rotation = rotation;
    }
}
