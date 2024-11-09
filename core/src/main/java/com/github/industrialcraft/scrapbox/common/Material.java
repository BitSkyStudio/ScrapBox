package com.github.industrialcraft.scrapbox.common;

import com.badlogic.gdx.graphics.Color;

public enum Material {
    Wood(0, "Wood", 0.8f, 1, true, new Color(120 / 255f, 60 / 255f, 6 / 255f, 1)),
    Metal(1, "Metal", 1.5f, 3, false, new Color(117 / 255f, 117 / 255f, 117 / 255f, 1));
    public final byte id;
    public final String name;
    public final float density;
    public final float baseHealthMultiplier;
    public final boolean flammable;
    public final Color color;
    Material(int id, String name, float density, float baseHealthMultiplier, boolean flammable, Color color) {
        this.id = (byte) id;
        this.name = name;
        this.density = density;
        this.baseHealthMultiplier = baseHealthMultiplier;
        this.flammable = flammable;
        this.color = color;
    }
    public static Material byId(byte id){
        for(Material material : values()){
            if(material.id == id)
                return material;
        }
        return null;
    }
}
