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
    public final float volume;
    public ClientSoundInstance(InGameScene scene, PlaySoundMessage message){
        this.serverId = message.id;
        this.sound = scene.sounds.get(message.sound);
        this.clientId = message.loop?sound.loop():sound.play();
        this.gameObject = scene.gameObjects.get(message.gameObjectId);
        this.offset = message.offset;
        this.volume = message.volume;
    }
    public void stop(){
        this.sound.stop(clientId);
    }
    public void tick(CameraController cameraController){
        Vector2 cameraPosition = new Vector2(cameraController.camera.position.x/InGameScene.BOX_TO_PIXELS_RATIO, cameraController.camera.position.y/InGameScene.BOX_TO_PIXELS_RATIO);
        //System.out.println(cameraPosition);
        float distance = 1/(gameObject.getRealPosition().add(offset).dst(cameraPosition)/10);
        float panDiff = (gameObject.getRealPosition().x-cameraPosition.x)/100;
        this.sound.setPan(clientId, panDiff, distance*Math.min(1, volume) * 0.2f);
        //System.out.println(panDiff + ":" + distance);
    }
    public boolean isStopped(){
        return !ScrapBox.getInstance().soundStateChecker.isPlaying((int) clientId);
    }
}
