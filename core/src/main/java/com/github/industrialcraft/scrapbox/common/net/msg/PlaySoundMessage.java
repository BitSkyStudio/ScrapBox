package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;

public class PlaySoundMessage {
    public final int id;
    public final String sound;
    public final int gameObjectId;
    public final Vector2 offset;
    public final boolean loop;

    public PlaySoundMessage(int id, String sound, int gameObjectId, Vector2 offset, boolean loop) {
        this.id = id;
        this.sound = sound;
        this.gameObjectId = gameObjectId;
        this.offset = offset;
        this.loop = loop;
    }
}
