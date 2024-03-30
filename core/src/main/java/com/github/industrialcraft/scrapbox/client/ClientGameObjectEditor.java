package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;

public class ClientGameObjectEditor {
    public final Window window;
    public ClientGameObjectEditor(SetGameObjectEditUIData data) {
        Skin skin = ScrapBox.getInstance().getSkin();
        this.window = new Window("go: "+data.id, skin);
        rebuild(data);
    }
    public void rebuild(SetGameObjectEditUIData data){
        this.window.clear();
        Skin skin = ScrapBox.getInstance().getSkin();
        Label label = new Label(data.text, skin);
        this.window.add(label);
    }
}
