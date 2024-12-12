package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.codedisaster.steamworks.*;

import java.io.File;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScrapBox extends ApplicationAdapter {
    private IScene scene;
    private Skin skin;
    public SoundStateChecker soundStateChecker;
    private SBSettings settings;
    public ScrapBox(SoundStateChecker soundStateChecker) {
        this.soundStateChecker = soundStateChecker;
    }
    @Override
    public void create() {
        settings = new SBSettings();
        skin = new Skin();
        skin.add("default-font", new BitmapFont(Gdx.files.internal("skin/default.fnt"), new TextureRegion(new Texture("skin/default.png"))));
        skin.add("big-font", new BitmapFont(Gdx.files.internal("skin/default-big.fnt"), new TextureRegion(new Texture("skin/default-big.png"))));
        skin.addRegions(new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas")));
        skin.load(Gdx.files.internal("skin/uiskin.json"));
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
    public static SBSettings getSettings(){
        return getInstance().settings;
    }

    public IScene getScene() {
        return this.scene;
    }
}
