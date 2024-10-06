package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DisconnectedScene extends StageBasedScreen {
    public final String reason;
    public DisconnectedScene(String reason) {
        this.reason = reason;
    }
    @Override
    public void create() {
        super.create();
        Skin skin = ScrapBox.getInstance().getSkin();
        Label connectingText = new Label("Disconnected: " + reason, skin);
        table.add(connectingText);
        table.row();
        TextButton back = new TextButton("back", skin);
        back.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new MainMenuScene());
            }
        });
        table.add(back);
    }
}
