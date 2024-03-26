package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.EGameObjectMode;

import java.util.ArrayList;

public class Vehicle {
    private final ArrayList<GameObject> gameObjects;
    private EGameObjectMode mode;
    public Vehicle() {
        this.gameObjects = new ArrayList<>();
        this.mode = EGameObjectMode.Normal;
    }
    public void add(GameObject gameObject){
        if(gameObject.vehicle == this){
            return;
        }
        if(gameObject.vehicle == null){
            this.gameObjects.add(gameObject);
            gameObject.vehicle = this;
        } else {
            for(GameObject go : gameObject.vehicle.gameObjects){
                this.gameObjects.add(go);
                go.setMode(this.mode);
                go.vehicle = this;
            }
        }
    }
    public EGameObjectMode getMode(){
        return this.mode;
    }
    public void setMode(EGameObjectMode mode){
        this.mode = mode;
        for(GameObject go : this.gameObjects){
            go.setMode(mode);
        }
    }
    public Vector2 getCenterOfMass(){
        float totalMass = 0.f;
        for (GameObject go : this.gameObjects) {
            totalMass += go.getMass();
        }
        Vector2 center = new Vector2();
        for (GameObject go : this.gameObjects) {
            Vector2 localCenter = go.getCenterOfMass();
            center.add(localCenter.scl(go.getMass() / totalMass));
        }
        return center;
    }
}
