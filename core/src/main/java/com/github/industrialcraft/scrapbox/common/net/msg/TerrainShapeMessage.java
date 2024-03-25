package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

import java.util.ArrayList;

public class TerrainShapeMessage extends MessageS2C {
    public final ArrayList<ArrayList<Vector2>> terrain;
    public TerrainShapeMessage(ArrayList<ArrayList<Vector2>> terrain) {
        this.terrain = terrain;
    }
}
