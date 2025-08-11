package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.game.ChestGameObject;
import com.github.industrialcraft.scrapbox.server.game.RopeGameObject;
import com.github.industrialcraft.scrapbox.server.game.StickGameObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class Vehicle {
    public final ArrayList<GameObject> gameObjects;
    private EObjectInteractionMode mode;
    public Vehicle() {
        this.gameObjects = new ArrayList<>();
        this.mode = EObjectInteractionMode.Normal;
    }
    public SaveFile.SavedVehicle save(){
        return new SaveFile.SavedVehicle(gameObjects.get(0).uuid, this.mode == EObjectInteractionMode.Static);
    }
    public void load(SaveFile.SavedVehicle vehicle){
        this.setMode(vehicle.isStatic?EObjectInteractionMode.Static:EObjectInteractionMode.Normal);
    }
    public void add(GameObject gameObject){
        if(gameObject.vehicle == this){
            return;
        }
        if(gameObject.vehicle == null){
            this.gameObjects.add(gameObject);
            gameObject.vehicle = this;
        } else {
            gameObject.vehicle.setMode(this.mode);
            for(GameObject go : gameObject.vehicle.gameObjects){
                this.gameObjects.add(go);
                go.vehicle = this;
            }
        }
    }
    public EObjectInteractionMode getMode(){
        return this.mode;
    }
    public void setMode(EObjectInteractionMode mode){
        this.mode = mode;
        for(GameObject go : this.gameObjects){
            go.setMode(mode);
            if(go instanceof IPairObject){
                GameObject other = ((IPairObject) go).getOther();
                if(other != null && mode != EObjectInteractionMode.Static && other.getLocalMode() != EObjectInteractionMode.Static)
                    other.setMode(mode);
                else if(other != null && other.getLocalMode() == EObjectInteractionMode.Ghost){
                    other.setMode(EObjectInteractionMode.Normal);
                }
            }
        }
    }
    public Vector2 getCenterOfMass(){
        float totalMass = getMass();
        Vector2 center = new Vector2();
        for (GameObject go : this.gameObjects) {
            Vector2 localCenter = go.getCenterOfMass();
            center.add(localCenter.scl(go.getMass() / totalMass));
        }
        return center;
    }
    public float getMass(){
        float totalMass = 0.f;
        for (GameObject go : this.gameObjects) {
            totalMass += go.getMass();
        }
        return totalMass;
    }
    public float countItem(EItemType item){
        float count = 0;
        for(GameObject go : this.gameObjects){
            if(go instanceof ChestGameObject){
                count += ((ChestGameObject) go).inventory.getOrDefault(item, 0f);
            }
        }
        return count;
    }
    public float removeItem(EItemType item, float count){
        for(GameObject go : this.gameObjects){
            if(go instanceof ChestGameObject){
                EnumMap<EItemType, Float> inventory = ((ChestGameObject) go).inventory;
                if(inventory.containsKey(item)) {
                    float removeCount = Math.min(count, inventory.get(item));
                    count -= removeCount;
                    inventory.put(item, inventory.get(item)-removeCount);
                    ((ChestGameObject) go).updateViewers();
                    if(count <= 0)
                        return 0;
                }
            }
        }
        return count;
    }
    public void addItem(EItemType item, float count){
        for(GameObject go : this.gameObjects){
            if(go instanceof ChestGameObject){
                EnumMap<EItemType, Float> inventory = ((ChestGameObject) go).inventory;
                inventory.put(item, inventory.getOrDefault(item, 0f) + count);
                ((ChestGameObject) go).updateViewers();
                return;
            }
        }
    }
}
