package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.ClientNetXConnection;
import com.github.industrialcraft.scrapbox.common.net.IConnection;

public class ConnectingScene extends StageBasedScreen {
    public final IConnection connection;
    public final NetXClient netXClient;
    public ConnectingScene(IConnection connection, NetXClient netXClient) {
        this.connection = connection;
        this.netXClient = netXClient;
    }

    @Override
    public void create() {
        super.create();
        Skin skin = ScrapBox.getInstance().getSkin();
        Label connectingText = new Label("Connecting...", skin);
        table.add(connectingText);
    }

    @Override
    public void render() {
        super.render();
        ((ClientNetXConnection)connection).waitForConnect();
    }
}
