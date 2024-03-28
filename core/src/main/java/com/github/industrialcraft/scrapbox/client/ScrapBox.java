package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScrapBox extends ApplicationAdapter {
    private IScene scene;
    private Skin skin;
    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        scene = new MainMenuScene();
        scene.create();
    }
    @Override
    public void render() {
        scene.render();
    }
    @Override
    public void resize(int width, int height) {
        scene.resize(width, height);
    }
    @Override
    public void dispose() {
        scene.dispose();
        skin.dispose();
    }
    public void setScene(IScene newScene){
        this.scene.dispose();
        this.scene = newScene;
        this.scene.create();
    }
    public Skin getSkin() {
        return skin;
    }
    public static ScrapBox getInstance(){
        return (ScrapBox) Gdx.app.getApplicationListener();
    }

    public IScene getScene() {
        return this.scene;
    }
}
