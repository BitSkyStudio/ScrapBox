package com.github.industrialcraft.scrapbox.client;

public interface IScene {
    void create();
    void render();
    void resize(int width, int height);
    void dispose();
}
