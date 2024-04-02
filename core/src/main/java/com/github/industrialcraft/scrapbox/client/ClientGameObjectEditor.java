package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;

public class ClientGameObjectEditor {
    public final int gameObjectID;
    public final Texture linkInput;
    public final Texture linkOutput;
    public final InGameScene scene;
    public final Window window;
    public ClientGameObjectEditor(int gameObjectID, InGameScene scene, SetGameObjectEditUIData data) {
        this.gameObjectID = gameObjectID;
        this.linkInput = new Texture("link_input.png");
        this.linkOutput = new Texture("link_output.png");
        this.scene = scene;
        Skin skin = ScrapBox.getInstance().getSkin();
        this.window = new Window("go: "+data.id, skin);
        this.window.padTop(50);
        window.setPosition(Gdx.input.getX(), Gdx.graphics.getWidth()-Gdx.input.getY());
        rebuild(data);
    }
    public void rebuild(SetGameObjectEditUIData data){
        this.window.clear();
        Skin skin = ScrapBox.getInstance().getSkin();
        for(int i = 0;i < data.rows.size();i++){
            data.rows.get(i).elements.forEach(editorUIElement -> window.add(editorUIElement.createActor(skin, this)));
            window.row();
        }
        TextButton close = new TextButton("close", skin);
        close.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scene.closeEditor(ClientGameObjectEditor.this);
            }
        });
        this.window.add(close);
        window.pack();
    }
    public void dispose(){
        this.linkInput.dispose();
        this.linkOutput.dispose();
    }
}
