package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.codedisaster.steamworks.*;

import java.io.File;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScrapBox extends ApplicationAdapter {
    private IScene scene;
    private Skin skin;
    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.get("default-font", BitmapFont.class).getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        scene = new MainMenuScene();
        scene.create();
        scene.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        new File("./saves").mkdir();
        /*try {
            SteamAPI.loadLibraries();
            if (!SteamAPI.init()) {
                System.out.println("couldnt initialize");
            }

        } catch (SteamException e) {
            // Error extracting or loading native libraries
        }*/
    }
    @Override
    public void render() {
        /*if (SteamAPI.isSteamRunning()) {
            SteamAPI.runCallbacks();
        }*/
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
        this.scene.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
