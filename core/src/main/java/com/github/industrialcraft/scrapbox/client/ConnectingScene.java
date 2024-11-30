package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.industrialcraft.netx.ClientMessage;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.DisconnectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameState;
import com.github.industrialcraft.scrapbox.common.net.msg.SubmitPassword;

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
        table.add(connectingText).row();
        TextButton back = new TextButton("back", skin);
        back.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new MainMenuScene());
            }
        });
        table.add(back);
    }

    @Override
    public void render() {
        super.render();
        ClientNetXConnection conn = ((ClientNetXConnection)connection);
        conn.client.visitMessage(new ClientMessage.Visitor() {
            @Override
            public void message(NetXClient user, Object msg) {
                if(msg instanceof SetGameState){
                    SetGameState setGameStateMessage = (SetGameState) msg;
                    if(setGameStateMessage.gameState == SetGameState.GameState.REQUEST_PASSWORD){
                        TextField input = new TextField("", ScrapBox.getInstance().getSkin());
                        Dialog passwordEnter = new Dialog("Enter Password", ScrapBox.getInstance().getSkin(), "dialog") {
                            @Override
                            protected void result(Object object) {
                                if (object instanceof String) {
                                    conn.send(new SubmitPassword(input.getText()));
                                } else {
                                    ScrapBox.getInstance().setScene(new ServerJoinScene());
                                }
                            }
                        };
                        passwordEnter.setMovable(false);
                        passwordEnter.button("Cancel");
                        passwordEnter.button("Ok", "");
                        passwordEnter.getContentTable().add(input);
                        passwordEnter.show(stage);
                    } else if(setGameStateMessage.gameState == SetGameState.GameState.PLAY){
                        ConnectingScene scene = (ConnectingScene) ScrapBox.getInstance().getScene();
                        ScrapBox.getInstance().setScene(new InGameScene(scene.connection, null, scene.netXClient));
                    }
                } else if(msg instanceof DisconnectMessage) {
                    ScrapBox.getInstance().setScene(new DisconnectedScene(((DisconnectMessage) msg).reason));
                } else {
                    throw new IllegalStateException("invalid message: " + msg);
                }
            }

            @Override
            public void exception(NetXClient user, Throwable exception) {
                ScrapBox.getInstance().setScene(new DisconnectedScene("connecting failed: " + exception.getLocalizedMessage()));
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        netXClient.disconnect();
    }
}
