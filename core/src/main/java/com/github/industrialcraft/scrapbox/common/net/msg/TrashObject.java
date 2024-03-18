package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class TrashObject extends MessageC2S {
    public final int id;
    public TrashObject(int id) {
        this.id = id;
    }
}
