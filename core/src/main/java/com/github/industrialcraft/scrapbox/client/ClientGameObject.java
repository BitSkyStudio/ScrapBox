package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.GameObject;

public class ClientGameObject {
    public final String type;
    public final int id;
    public Vector2 lastPosition;
    public Vector2 position;
    public float rotation;
    public float lastRotation;
    public EObjectInteractionMode mode;
    public int selectionId;
    public boolean selected;
    public int lastUpdateLength;
    public long lastUpdate;
    public ClientWorldManager.AnimationData animationData;
    public ClientWorldManager.AnimationData lastAnimationData;
    public Object internalRendererData;
    public float maxHealth;
    public float health;
    public final GameObject.GameObjectConfig config;
    public final boolean gearJoinable;
    public ClientGameObject(AddGameObjectMessage message) {
        this.type = message.type;
        this.id = message.id;
        this.lastPosition = message.position;
        this.position = message.position;
        this.lastRotation = message.rotation;
        this.rotation = message.rotation;
        this.selectionId = message.selectionId;
        this.mode = EObjectInteractionMode.Normal;
        this.selected = false;
        this.lastUpdate = System.currentTimeMillis();
        this.lastUpdateLength = 0;
        this.animationData = message.animation;
        this.lastAnimationData = message.animation;
        this.internalRendererData = null;
        this.maxHealth = message.maxHealth;
        this.health = message.health;
        this.config = message.config;
        this.gearJoinable = message.gearJoinable;
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
        this.lastAnimationData = this.animationData;
        this.animationData = message.animation;
        this.health = message.health;
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
    public String getAnimationString(String name, String defaultString){
        return animationData.getString(name, defaultString);
    }
    public float getAnimationNumber(String name, float defaultNumber){
        float newNumber = animationData.getNumber(name, defaultNumber);
        float oldNumber = lastAnimationData.getNumber(name, newNumber);
        return MathUtils.lerp(oldNumber, newNumber, getProgress());
    }
}
