package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;

public class ClientGameObject {
    public final String type;
    public Vector2 lastPosition;
    public Vector2 position;
    public float rotation;
    public float lastRotation;
    public EObjectInteractionMode mode;
    public boolean selectable;
    public boolean selected;
    public int lastUpdateLength;
    public long lastUpdate;
    public String animationData;
    public ClientGameObject(AddGameObjectMessage message) {
        this.type = message.type;
        this.lastPosition = message.position;
        this.position = message.position;
        this.lastRotation = message.rotation;
        this.rotation = message.rotation;
        this.selectable = message.selectable;
        this.mode = EObjectInteractionMode.Normal;
        this.selected = false;
        this.lastUpdate = System.currentTimeMillis();
        this.lastUpdateLength = 0;
        this.animationData = message.animation;
    }
    public void move(MoveGameObjectMessage message){
        this.lastPosition = this.position;
        this.position = message.position;
        this.lastRotation = this.rotation;
        this.rotation = message.rotation;
        this.mode = message.mode;
        this.selected = message.selected;
        this.lastUpdateLength = (int) (System.currentTimeMillis() - this.lastUpdate);
        this.lastUpdate = System.currentTimeMillis();
        this.animationData = message.animation;
    }
    private float getProgress(){
        return ((float)(System.currentTimeMillis() - this.lastUpdate))/this.lastUpdateLength;
    }
    public Vector2 getRealPosition(){
        return this.lastPosition.cpy().lerp(this.position, getProgress());
    }
    public float getRealAngle(){
        return MathUtils.lerpAngle(lastRotation, rotation, getProgress());
    }
}
