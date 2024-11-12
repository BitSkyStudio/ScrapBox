package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.PlaySoundMessage;

public class ClientSoundInstance {
    public final int serverId;
    public final long clientId;
    public final ClientGameObject gameObject;
    public final Vector2 offset;
    public final Sound sound;
    public ClientSoundInstance(InGameScene scene, PlaySoundMessage message){
        this.serverId = message.id;
        this.sound = scene.sounds.get(message.sound);
        this.clientId = message.loop?sound.loop():sound.play();
        this.gameObject = scene.gameObjects.get(message.gameObjectId);
        this.offset = message.offset;
    }
    public void stop(){
        this.sound.stop(clientId);
    }
    public boolean isStopped(){
        return !ScrapBox.getInstance().soundStateChecker.isPlaying((int) clientId);
    }
}
