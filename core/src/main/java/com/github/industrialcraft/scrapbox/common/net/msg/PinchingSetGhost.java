package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class PinchingSetGhost extends MessageC2S {
    public final boolean isGhost;
    public PinchingSetGhost(boolean isGhost) {
        this.isGhost = isGhost;
    }
}
