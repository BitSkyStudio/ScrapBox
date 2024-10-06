package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;

public interface IPairObject {
    void changeDistance(float by);
    GameObject getOther();
    Vector2 getPairJointOffset();
}
