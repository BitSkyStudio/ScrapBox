package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Vehicle {
    private final ArrayList<GameObject> gameObjects;
    public Vehicle() {
        this.gameObjects = new ArrayList<>();
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
                go.vehicle = this;
            }
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
