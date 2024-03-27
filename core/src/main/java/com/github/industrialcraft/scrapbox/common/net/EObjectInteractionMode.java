package com.github.industrialcraft.scrapbox.common.net;

public enum EObjectInteractionMode {
    Normal(0),
    Static(1),
    Ghost(2);
    public final byte id;
    EObjectInteractionMode(int id) {
        this.id = (byte) id;
    }
    public static EObjectInteractionMode fromId(byte id){
        for(EObjectInteractionMode mode : values()){
            if(mode.id == id){
                return mode;
            }
        }
        return null;
    }
}
