package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;

public class ClientGameObject {
    public final String type;
    public Vector2 position;
    public float rotation;
    public EObjectInteractionMode mode;
    public boolean selected;
    public ClientGameObject(AddGameObjectMessage message) {
        this.type = message.type;
        this.position = message.position;
        this.rotation = message.rotation;
        this.mode = EObjectInteractionMode.Normal;
        this.selected = false;
    }
    public void move(MoveGameObjectMessage message){
        this.position = message.position;
        this.rotation = message.rotation;
        this.mode = message.mode;
        this.selected = message.selected;
    }
}
