package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

public class TakeObjectResponse extends MessageS2C {
    public final int id;
    public final Vector2 offset;
    public TakeObjectResponse(int id, Vector2 offset) {
        this.id = id;
        this.offset = offset;
    }
}
