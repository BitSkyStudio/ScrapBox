package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

public class DeleteGameObject extends MessageS2C {
    public final int id;
    public DeleteGameObject(int id) {
        this.id = id;
    }
}
