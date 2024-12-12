package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.server.SaveFile;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.FileInputStream;

public class MainMenuScene extends StageBasedScreen{
    @Override
    public void create() {
        super.create();
        Skin skin = ScrapBox.getInstance().getSkin();
        TextButton startGameButton = new TextButton("Start Game", skin, "big");
        startGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new WorldJoinScene());
            }
        });
        table.add(startGameButton);
        table.row();
        TextButton serverListButton = new TextButton("Server List", skin, "big");
        serverListButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new ServerJoinScene());
            }
        });
        table.add(serverListButton);
        table.row();
        TextButton settingsButton = new TextButton("Settings", skin, "big");
        settingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new SettingsScene());
            }
        });
        table.add(settingsButton);
        table.row();
        TextButton exitListButton = new TextButton("Exit", skin, "big");
        exitListButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.exit(0);
            }
        });
        table.add(exitListButton);
    }
}
