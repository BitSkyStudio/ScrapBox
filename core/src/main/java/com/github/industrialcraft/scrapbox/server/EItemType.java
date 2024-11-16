package com.github.industrialcraft.scrapbox.server;

public enum EItemType {
    Wood(0, "Wood"),
    Metal(1, "Metal"),
    Circuit(2, "Circuit"),
    Transmission(3, "Transmission"),
    StickyResin(4, "Sticky Resin"),
    Explosive(5, "Explosive");
    public final byte id;
    public final String name;
    EItemType(int id, String name) {
        this.id = (byte) id;
        this.name = name;
    }
    public static EItemType byId(byte id){
        for(EItemType item : values()){
            if(item.id == id)
                return item;
        }
        return null;
    }
}
