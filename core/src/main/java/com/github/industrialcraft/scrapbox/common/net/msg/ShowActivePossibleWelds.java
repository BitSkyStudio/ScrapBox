package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

import java.util.ArrayList;

public class ShowActivePossibleWelds extends MessageS2C {
    public final ArrayList<PossibleWeld> welds;
    public ShowActivePossibleWelds(ArrayList<PossibleWeld> welds) {
        this.welds = welds;
    }
    public static class PossibleWeld{
        public final Vector2 first;
        public final Vector2 second;
        public PossibleWeld(Vector2 first, Vector2 second) {
            this.first = first;
            this.second = second;
        }
    }
}
