package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.industrialcraft.netx.LanReceiver;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.common.net.MessageRegistryCreator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;

public class SettingsScene extends StageBasedScreen {
    @Override
    public void create() {
        super.create();
        table.add(ScrapBox.getSettings().createTable(null)).row();
        TextButton back = new TextButton("Back", ScrapBox.getInstance().getSkin());
        back.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new MainMenuScene());
            }
        });
        table.add(back);
    }
}
